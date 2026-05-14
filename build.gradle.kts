import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    java
    kotlin("jvm") version "2.1.0" apply false
    kotlin("kapt") version "2.1.0" apply false
    kotlin("plugin.spring") version "2.1.0" apply false
    kotlin("plugin.jpa") version "2.1.0" apply false
    id("org.springframework.boot") version "3.5.14" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false

    // R-45 (평가 심화 (2)-1): SonarCloud 정적 분석 + 커버리지 게이트.
    id("org.sonarqube") version "5.1.0.4882"
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "ktcloud-cloudnative-troica-team")
        property("sonar.projectKey", "KTCloud-CloudNative-Troica-Team_msa-inventory-service")
        property("sonar.qualitygate.wait", "true")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "**/build/reports/jacoco/test/jacocoTestReport.xml",
        )
        property("sonar.exclusions", "**/generated/**, **/build/**")
    }
}

allprojects {
    group = "com.troica.msa"
    version = providers.gradleProperty("version").get()

    repositories {
        mavenCentral()
        // 로컬 dev: ./gradlew publishToMavenLocal 한 common-libs 사용
        mavenLocal()
        // 정식 경로: GitHub Packages (common-libs v0.3.0 stable)
        maven {
            name = "GitHubPackagesCommonLibs"
            url = uri("https://maven.pkg.github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: providers.gradleProperty("gpr.user").orNull
                password = System.getenv("GITHUB_TOKEN")
                    ?: providers.gradleProperty("gpr.token").orNull
            }
        }
        // JitPack (client-redis — D2)
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    // R-45: JaCoCo로 커버리지 측정 → SonarCloud가 XML report 읽음.
    apply(plugin = "jacoco")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            // -Xskip-metadata-version-check: JitPack client-redis(v1.0.2)가 Kotlin 2.3.x metadata로 컴파일됨.
            // 우리 Kotlin 2.1.0(R-16 회피용 고정) 컴파일러는 metadata 2.3을 거부 → build fail.
            // 본 플래그로 metadata version check만 skip. wire-level/.class는 호환되므로 안전.
            // 팀장님이 JitPack v1.0.2를 Kotlin 2.1.x로 재컴파일하면 제거 가능.
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xskip-metadata-version-check")
        }
        jvmToolchain(21)
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.14")
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    }

    // kapt 적용 모듈에서 sourcesJar가 kaptKotlin 출력에 의존해야 Gradle 8.10의 입력 검증을 통과한다
    plugins.withId("org.jetbrains.kotlin.kapt") {
        tasks.matching { it.name == "sourcesJar" }.configureEach {
            dependsOn("kaptKotlin")
        }
    }

    // R-45: test → jacocoTestReport → Sonar XML report 자동 생성.
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy(tasks.matching { it.name == "jacocoTestReport" })
    }
    tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().configureEach {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
