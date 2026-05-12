pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // JitPack — D2 결정: client-redis는 팀장님의 JitPack 패키지 사용
        // (com.github.kanei0415:ktcloud-msa-client-redis:v1.0.2)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "msa-inventory-service"

include(
    "inventory",
    "inventory-service",
    "inventory-event",
)
