package dev.ktcloud.black.inventory.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

// Spring Boot 3.x부터 `@EnableBatchProcessing`은 기본 활성화 (auto-config). 명시 어노테이션 불필요.
@EnableTransactionManagement
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = ["dev.ktcloud.black"])
class InventoryServiceApplication


fun main(args: Array<String>) {
    runApplication<InventoryServiceApplication>(*args)
}
