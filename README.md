# msa-inventory-service

Troica Market Service의 **재고 도메인 + Event Sourcing + Saga 참여** 마이크로서비스.

> Single source of truth: [TROICA_SPEC.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/TROICA_SPEC.md) + [PHASE_4_RUNBOOK.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/PHASE_4_RUNBOOK.md)

## 모듈 구조

```
msa-inventory-service/
├── inventory/             # 도메인 라이브러리 (JPA, Kafka producer/consumer, Redis 분산락)
├── inventory-event/       # Event Sourcing 이벤트 스토어 (append-only InventoryEvent)
└── inventory-service/     # Spring Boot 앱 (gRPC + Spring Batch worker)
```

## 두 가지 K8s Deployment (동일 이미지, 다른 프로파일)

| Deployment | `SPRING_PROFILES_ACTIVE` | 동작 |
|------------|------------------------|------|
| `inventory-service` (api) | `dev` 또는 `prod` | gRPC + REST + Kafka consumer(`order.pending`) + producer(`order.inventory-reserved`) |
| `inventory-service-worker` | `dev,worker` 또는 `prod,worker` | Spring Batch Job 실행 (InventoryEvent projection) — Q7 |

## 포트 / 의존성

| 항목 | 값 |
|------|----|
| HTTP / Actuator | 8003 |
| gRPC | 9003 |
| PostgreSQL | `inventory_db` |
| Redis | `inventory-service-redis` (분산락 + 멱등 처리) |
| Kafka | consumer `order.pending`, producer `order.inventory-reserved` |

## 의존성 (D2/D3 결정 반영)

```kotlin
// inventory 모듈
implementation("com.troica.msa:common:0.3.0")
implementation("com.github.kanei0415:ktcloud-msa-client-redis:v1.0.2")  // JitPack
```

`settings.gradle.kts`에 `maven { url = uri("https://jitpack.io") }` 필수.

## Q7 — Event Sourcing + Spring Batch projection

- `inventory-event` 모듈은 모든 재고 mutation 이벤트(`INCREMENT`/`DECREMENT`)를 append-only로 기록
- worker 프로파일 기동 시 `InventoryEventProjectionBatchConfig`의 Job이 자동 실행
- chunk-oriented ItemReader → Processor → Writer (PoC는 PROCESSED 마킹 + logging)
- 추후 read model(일별 변동 집계, audit log, ES indexing 등) projection 로직 확장 지점

## 빌드 + 실행

```bash
# common-libs를 로컬에 한 번 publish
(cd ../msa-common-libs && ./gradlew publishToMavenLocal -Pversion=0.3.0)

# 빌드
./gradlew build -x test

# Docker 이미지
docker build \
  --build-arg GPR_USER=$GITHUB_ACTOR \
  --build-arg GPR_TOKEN=$GITHUB_TOKEN \
  -t msa/inventory-service:local .

# api 컨테이너
docker run --rm -p 8003:8003 -p 9003:9003 \
  -e SPRING_PROFILES_ACTIVE=dev \
  msa/inventory-service:local

# worker 컨테이너
docker run --rm \
  -e SPRING_PROFILES_ACTIVE=dev,worker \
  msa/inventory-service:local
```
