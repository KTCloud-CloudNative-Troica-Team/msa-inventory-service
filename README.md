# msa-inventory-service

Troica Market Service의 **재고 도메인 + Event Sourcing + Spring Batch worker** 마이크로서비스.

> SPEC + ADR ([ADR-0007](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/adr/0007-inventory-event-sourcing-batch-worker.md)): [msa-argocd-manifest/docs](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/tree/main/docs)
> 트러블슈팅: [TROUBLESHOOTING.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md)

---

## 빠른 시작 (L2 — 로컬 docker로 끝까지 실행)

### 사전 요구사항

| 항목 | 버전 |
|---|---|
| Java | 21 (Temurin) |
| Docker | 24+ |
| GitHub PAT | `read:packages` |

### 1. GH Packages 인증 (1회)

`~/.gradle/gradle.properties`:
```
gpr.user=<github-username>
gpr.token=<PAT-with-read:packages>
```

### 2. PostgreSQL + Redis + Kafka 컨테이너 띄우기

```bash
docker run -d --name pg-inventory \
  -p 7004:5432 \
  -e POSTGRES_USER=inventory-service \
  -e POSTGRES_PASSWORD=inventory-service \
  -e POSTGRES_DB=inventory_db \
  postgres:18-alpine

docker run -d --name redis-inventory \
  -p 7005:6379 \
  redis:8.6-alpine \
  redis-server --requirepass inventory-service

# Kafka (msa-order-service와 공유 가능, 같은 broker 사용)
docker run -d --name kafka-shared \
  -p 7003:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://host.docker.internal:7003 \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  apache/kafka:4.2.0

# 토픽 (ADR-0004 SPEC 표준)
sleep 10
for topic in order.pending order.inventory-reserved; do
  docker exec kafka-shared /opt/kafka/bin/kafka-topics.sh \
    --create --bootstrap-server localhost:9092 \
    --replication-factor 1 --partitions 3 --topic "$topic"
done
```

### 3. 빌드 + 테스트

```bash
./gradlew build
```

### 4. 로컬 실행 — api Pod

```bash
./gradlew :inventory-service:bootRun --args='--spring.profiles.active=dev'
```

기대:
```
Started InventoryServiceApplicationKt in X seconds
Tomcat started on port 8003 (http)
gRPC server started on port 9003
```

### 5. 로컬 실행 — worker Pod (Spring Batch, 별도 터미널)

```bash
./gradlew :inventory-service:bootRun --args='--spring.profiles.active=dev,worker'
```

worker는 `InventoryEventProjectionBatchConfig` Job을 실행:
- Kafka `order.pending` consume → inventory 예약 시도 → `inventory_event` append → `order.inventory-reserved` 발행

### 6. 검증

```bash
# api Pod
curl -s http://localhost:8003/healthz | jq

# gRPC
grpcurl -plaintext localhost:9003 list

# Kafka 메시지 흐름 (별도 터미널)
docker exec kafka-shared /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic order.inventory-reserved --from-beginning
```

### 7. Docker로 실행

```bash
./gradlew :inventory-service:bootJar
docker build -t msa/inventory-service:local .

# api
docker run --rm \
  -p 8003:8003 -p 9003:9003 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e INVENTORY_DB_HOST=host.docker.internal \
  -e REDIS_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:7003 \
  msa/inventory-service:local

# worker (별도 컨테이너)
docker run --rm \
  -e SPRING_PROFILES_ACTIVE=dev,worker \
  -e INVENTORY_DB_HOST=host.docker.internal \
  -e REDIS_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:7003 \
  msa/inventory-service:local
```

### 8. 정리

```bash
docker rm -f pg-inventory redis-inventory kafka-shared
```

---

## 모듈 구조 (Event Sourcing 패턴)

```
msa-inventory-service/
├── inventory/                # 도메인 — Inventory aggregate, port interfaces
├── inventory-event/          # Event Sourcing — append-only event store + 누적 계산
└── inventory-service/        # Spring Boot 앱
    ├── adapter/web/inbound/      # gRPC controller (재고 조회)
    ├── worker/batch/             # @Profile("worker") Spring Batch Job/Step
    └── kafka/                    # Producer (order.inventory-reserved)
```

---

## Event Sourcing (ADR-0007)

- `inventory_event` 테이블 = **append-only event store** (immutable)
- 도메인 이벤트: `InventoryReserved`, `InventoryReleased`, `InventoryReplenished`
- **현재 재고 = 이벤트 누적 적용** (snapshot 캐시는 Phase 5+ 최적화)
- worker가 Spring Batch chunk 단위로 처리 (`ItemReader` Kafka → `ItemProcessor` 도메인 → `ItemWriter` 이벤트 store append)

---

## Kafka 토픽 (ADR-0003 wire=JSON, ADR-0004 표준명)

| 토픽 | 본 서비스 역할 |
|---|---|
| `order.pending` | **consumer** (Spring Batch reader) |
| `order.inventory-reserved` | **producer** (재고 예약 성공 시) |

---

## 포트

| 프로토콜 | 포트 (api Pod) | worker Pod |
|---------|---------------|------------|
| HTTP | 8003 | (미노출) |
| gRPC | 9003 | (미노출) |

worker는 `spring.main.web-application-type=none` + `grpc.server.port=-1`.

---

## 환경 변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | (none) | `dev` (api) / `dev,worker` (worker) |
| `INVENTORY_DB_HOST` | localhost | PostgreSQL host |
| `INVENTORY_DB_PORT` | 7004 | PostgreSQL port |
| `INVENTORY_DB_NAME` | inventory_db | DB name |
| `INVENTORY_DB_USERNAME` | inventory-service | DB user |
| `INVENTORY_DB_PASSWORD` | inventory-service | DB password |
| `REDIS_HOST` | localhost | Redis (snapshot cache) |
| `REDIS_PORT` | 7005 | Redis port |
| `REDIS_PASSWORD` | inventory-service | Redis password |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:7003 | Kafka bootstrap |
| `KAFKA_TOPIC_ORDER_PENDING` | order.pending | consume |
| `KAFKA_TOPIC_ORDER_INVENTORY_RESERVED` | order.inventory-reserved | produce |
| `SERVER_PORT` | 8003 | HTTP listen (api Pod) |
| `GRPC_SERVER_PORT` | 9003 | gRPC listen (api Pod) |

---

## 외부 의존성

| 의존 | 용도 | 로컬 |
|------|------|------|
| PostgreSQL `inventory_db` | event store + JPA | `postgres:18-alpine` |
| Redis | snapshot cache (재고 누적 계산 가속) | `redis:8.6-alpine` |
| Kafka | 도메인 이벤트 producer/consumer | `apache/kafka:4.2.0` (KRaft) |
| `com.troica.msa:common:0.3.1` | JPA/QueryDSL, 공통 예외 | GH Packages 자동 |
| `com.troica.msa:events:0.3.1` | Protobuf 페이로드 | GH Packages 자동 |
| `com.github.kanei0415:ktcloud-msa-client-redis:v1.0.2` | Redis client wrapper (ADR-0002) | JitPack |

---

## CI/CD

`.github/workflows/ci.yml`: PR build-test / push main → ECR + manifest auto-bump.

빌드 시간: ~2-3분 (R-27 (a) 적용).

---

## 트러블슈팅

- **JitPack client-redis Kotlin metadata 호환** → `-Xskip-metadata-version-check` 컴파일러 인자 (이미 build.gradle.kts에 적용). [TROUBLESHOOTING §1.3](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#13-jitpack-client-redis-kotlin-metadata-호환)
- **Spring Batch `ItemWriter` Kotlin lambda 타입 추론 실패** → `object : ItemWriter<...>` 명시 (적용됨). [TROUBLESHOOTING §1.5](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#15-spring-batch-itemwriter-kotlin-lambda-타입-추론-실패)
- **`:inventory-event` transitive 미인식** → `implementation(project(":inventory-event"))` 명시 (적용됨). [TROUBLESHOOTING §1.6](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#16-inventory-service가-inventory-event-모듈-transitive-미인식)
- **`@Configuration class may not be final`** → common-libs 0.3.1+ 사용 ([TROUBLESHOOTING §1.7](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#17-kotlin-configuration-class가-final--spring-cglib-proxy-실패-r-38))

---

## 관련 문서

- [msa-argocd-manifest](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest) — `applications/values/inventory-service/`
- [ADR-0002](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/adr/0002-client-libraries-distribution.md) — JitPack client-redis
- [ADR-0007](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/adr/0007-inventory-event-sourcing-batch-worker.md) — Event Sourcing + Spring Batch
- [msa-order-service](https://github.com/KTCloud-CloudNative-Troica-Team/msa-order-service) — `order.pending` producer / `order.inventory-reserved` consumer
- [TROUBLESHOOTING.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md)
