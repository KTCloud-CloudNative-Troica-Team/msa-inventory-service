package dev.ktcloud.black.inventory.adapter.infrastructure.redis

import dev.ktcloud.black.inventory.application.port.cache.outbound.InventoryCacheCommandOutboundPort
import dev.ktcloud.black.inventory.adapter.configuration.redis.RedisConfig
import dev.ktcloud.black.inventory.domain.exception.InventoryException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class InventoryRedisCommandAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
    private val decreaseInventoryScript: RedisScript<Long>,
    private val increaseInventoryScript: RedisScript<Long>,
    private val setInventoryScript: RedisScript<Long>,
): InventoryCacheCommandOutboundPort {
    override fun decrease(inventoryId: Long, amount: Int, eventId: Long): Int {
        val redisKey = InventoryRedisKey(inventoryId).toRedisKey()

        val result = redisTemplate.execute(
            decreaseInventoryScript,
            listOf(redisKey),
            amount.toString(), eventId.toString()
        ).toInt()

        if (result == RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode)
            throw InventoryException.NoCachedInventoryFound()

        if (result == RedisConfig.InventoryScriptError.INVENTORY_NOT_ENOUGH.errorCode)
            throw InventoryException.InventoryNotEnough()

        return result
    }

    override fun increase(inventoryId: Long, amount: Int, eventId: Long): Int {
        val redisKey = InventoryRedisKey(inventoryId).toRedisKey()

        val result = redisTemplate.execute(
            increaseInventoryScript,
            listOf(redisKey),
            amount.toString(), eventId.toString()
        ).toInt()

        if (result == RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode)
            throw InventoryException.NoCachedInventoryFound()

        return result
    }

    override fun setInventoryQuantity(inventoryId: Long, quantity: Int, eventId: Long): Int {
        val redisKey = InventoryRedisKey(inventoryId).toRedisKey()

        val result = redisTemplate.execute(
            setInventoryScript,
            listOf(redisKey),
            quantity.toString(), eventId.toString()
        ).toInt()

        if (result == RedisConfig.InventoryScriptError.INVENTORY_DATA_STALE.errorCode)
            throw InventoryException.InventoryDataStale()

        return result
    }
}