package dev.ktcloud.black.inventory.adapter.configuration.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.RedisScript

@Configuration
class RedisConfig {
    enum class InventoryScriptError(val errorCode: Int) {
        NO_CACHED_INVENTORY_FOUND(-1), INVENTORY_NOT_ENOUGH(-2), INVENTORY_DATA_STALE(-3);
    }

    @Bean
    fun decreaseInventoryScript(): RedisScript<Long> {
        val resource = ClassPathResource("scripts/redis/decrease_inventory.lua")
        return RedisScript.of(resource, Long::class.java)
    }

    @Bean
    fun increaseInventoryScript(): RedisScript<Long> {
        val resource = ClassPathResource("scripts/redis/increase_inventory.lua")
        return RedisScript.of(resource, Long::class.java)
    }

    @Bean
    fun setInventoryScript(): RedisScript<Long> {
        val resource = ClassPathResource("scripts/redis/set_inventory.lua")
        return RedisScript.of(resource, Long::class.java)
    }
}