plugins {
    kotlin("plugin.jpa")
}

dependencies {
    // common-libs v0.3.0
    implementation("com.troica.msa:common:0.3.1")
    // JitPack client-redis (D2 결정)
    implementation("com.github.kanei0415:ktcloud-msa-client-redis:v1.0.2")
    // 같은 레포 내 서브모듈
    implementation(project(":inventory-event"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")

    runtimeOnly("com.h2database:h2")

    // R-57: 단위 테스트 — JUnit 5 + AssertJ + Mockito
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
