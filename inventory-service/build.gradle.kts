import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("com.google.protobuf") version "0.9.5"
}

object Versions {
    const val GRPC = "1.75.0"
    const val GRPC_KOTLIN = "1.4.1"
    const val PROTOBUF = "4.34.1"
}

dependencies {
    implementation(project(":inventory"))
    // Spring Batch Job/Step이 inventory-event의 repository/port를 직접 사용 — implementation 의존 명시
    // (:inventory가 :inventory-event를 implementation 한 transitive는 compile classpath 미노출)
    implementation(project(":inventory-event"))

    implementation("com.troica.msa:common:0.3.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Q7 — Spring Batch worker (event sourcing projection)
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // gRPC server + protobuf
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
    implementation("com.google.protobuf:protobuf-java:${Versions.PROTOBUF}")
    implementation("com.google.protobuf:protobuf-kotlin:${Versions.PROTOBUF}")
    implementation("io.grpc:grpc-protobuf:${Versions.GRPC}")
    implementation("io.grpc:grpc-stub:${Versions.GRPC}")
    implementation("io.grpc:grpc-kotlin-stub:${Versions.GRPC_KOTLIN}")
    implementation("io.grpc:grpc-netty-shaded:${Versions.GRPC}")

    // Netty CVE override — Lettuce(Redis) + Kafka client가 netty-codec/dns 4.1.132 가져옴.
    //   CVE-2026-42583 (Lz4FrameDecoder DoS) fix in 4.1.133.Final
    //   CVE-2026-42579 (DNS codec input validation bypass) fix in 4.1.133.Final
    implementation("io.netty:netty-codec:4.1.133.Final")
    implementation("io.netty:netty-codec-dns:4.1.133.Final")

    runtimeOnly("org.postgresql:postgresql:42.7.11")
}

sourceSets {
    main {
        java.srcDirs(
            "build/generated/source/proto/main/java",
            "build/generated/source/proto/main/kotlin",
        )
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.PROTOBUF}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.GRPC}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Versions.GRPC_KOTLIN}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("kotlin")
            }
            task.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

springBoot {
    mainClass.set("dev.ktcloud.black.inventory.service.InventoryServiceApplicationKt")
}
