plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
}

dependencies {
    implementation("com.troica.msa:common:0.3.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    runtimeOnly("com.h2database:h2")

    // QueryDSL 5.1 (모노레포 최신 정렬, kapt 제거 방향이지만 InventoryEvent용 Q-클래스 생성에 필요)
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("jakarta.annotation:jakarta.annotation-api")
}

sourceSets {
    main {
        kotlin.srcDir("build/generated/source/kapt/main")
    }
}
