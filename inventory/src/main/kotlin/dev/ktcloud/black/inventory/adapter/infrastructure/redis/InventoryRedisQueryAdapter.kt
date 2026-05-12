package dev.ktcloud.black.inventory.adapter.infrastructure.redis

import dev.ktcloud.black.inventory.adapter.configuration.redis.RedisConfig
import dev.ktcloud.black.inventory.application.port.cache.outbound.InventoryCacheQueryOutboundPort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class InventoryRedisQueryAdapter(
    private val redisTemplate: RedisTemplate<String, String>
): InventoryCacheQueryOutboundPort {
    override fun fetchInventory(inventoryId: Long): Int {
        val key = InventoryRedisKey(inventoryId).toRedisKey()

        val quantity = redisTemplate.opsForHash<String, String>().get(key, "quantity")

        return quantity?.toInt() ?: RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode
    }
}