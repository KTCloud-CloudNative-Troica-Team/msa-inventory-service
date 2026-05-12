package dev.ktcloud.black.inventory.adapter.infrastructure

import dev.ktcloud.black.inventory.adapter.configuration.redis.RedisConfig
import dev.ktcloud.black.inventory.application.port.cache.outbound.InventoryCacheCommandOutboundPort
import dev.ktcloud.black.inventory.application.port.cache.outbound.InventoryCacheQueryOutboundPort
import dev.ktcloud.black.inventory.application.port.outbound.LoadCacheSyncedInventoryOutboundPort
import dev.ktcloud.black.inventory.application.port.state.outbound.InventoryStateQueryOutboundPort
import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity
import dev.ktcloud.black.inventory.domain.exception.InventoryException
import dev.ktcloud.black.inventory.event.application.port.outbound.InventoryEventQueryOutboundPort
import org.springframework.stereotype.Component

@Component
class LoadCacheSyncedInventoryPersistenceAdapter(
    private val inventoryStateQueryOutboundPort: InventoryStateQueryOutboundPort,
    private val inventoryCacheQueryOutboundPort: InventoryCacheQueryOutboundPort,
    private val inventoryCacheCommandOutboundPort: InventoryCacheCommandOutboundPort,
    private val inventoryEventQueryOutboundPort: InventoryEventQueryOutboundPort
): LoadCacheSyncedInventoryOutboundPort {
    private fun calcCurrentQuantity(stateQuantity: Int, inventoryId: Long): Pair<Int, Long> {
        val unprocessedInventoryEvents = inventoryEventQueryOutboundPort.fetchUnprocessedEvents(inventoryId)

        val calculatedQuantity = stateQuantity + unprocessedInventoryEvents.sumOf { it.amount }

        val lastEventId = try{
            unprocessedInventoryEvents.last().id
        } catch (_: NoSuchElementException) {
            0
        }

        return Pair(calculatedQuantity, lastEventId)
    }

    private fun cacheReread(inventoryId: Long): Int {
        val cacheFetchResult = inventoryCacheQueryOutboundPort.fetchInventory(inventoryId)

        if (cacheFetchResult == RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode)
            throw InventoryException.InventoryCacheUnreadable()

        return cacheFetchResult
    }

    override fun loadCacheSyncedInventory(
        productId: String,
        skuCode: String
    ): InventoryDomainEntity {
        val inventoryState = inventoryStateQueryOutboundPort.fetch(
            productId = productId,
            skuCode = skuCode
        )

        val cacheFetchResult = inventoryCacheQueryOutboundPort.fetchInventory(inventoryState.id)

        if (cacheFetchResult == RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode) {
            val (currentQuantity, lastEventId) = calcCurrentQuantity(
                inventoryState.quantity,
                inventoryState.id
            )

            val cacheUpdateResult = inventoryCacheCommandOutboundPort.setInventoryQuantity(
                inventoryId = inventoryState.id,
                quantity = currentQuantity,
                eventId = lastEventId
            )

            if (cacheUpdateResult == RedisConfig.InventoryScriptError.INVENTORY_DATA_STALE.errorCode)
                inventoryState.setQuantity(cacheReread(inventoryState.id))
            else
                inventoryState.setQuantity(cacheUpdateResult)

            return inventoryState
        }

        inventoryState.setQuantity(cacheFetchResult)

        return inventoryState
    }

    override fun loadCacheSyncedInventory(inventoryId: Long): InventoryDomainEntity {
        val inventoryState = inventoryStateQueryOutboundPort.fetch(inventoryId)

        val cacheFetchResult = inventoryCacheQueryOutboundPort.fetchInventory(inventoryId)

        if (cacheFetchResult == RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode) {
            val (currentQuantity, lastEventId) = calcCurrentQuantity(
                inventoryState.quantity,
                inventoryId
            )

            val cacheUpdateResult = inventoryCacheCommandOutboundPort.setInventoryQuantity(
                inventoryId = inventoryId,
                quantity = currentQuantity,
                eventId = lastEventId
            )

            if (cacheUpdateResult == RedisConfig.InventoryScriptError.INVENTORY_DATA_STALE.errorCode)
                inventoryState.setQuantity(cacheReread(inventoryId))
            else
                inventoryState.setQuantity(cacheUpdateResult)

            return inventoryState
        }

        inventoryState.setQuantity(cacheFetchResult)

        return inventoryState
    }

    override fun loadInventory(
        productId: String,
        skuCode: String
    ): InventoryDomainEntity {
        val inventoryState = inventoryStateQueryOutboundPort.fetch(
            productId = productId,
            skuCode = skuCode
        )

        val cacheFetchResult = inventoryCacheQueryOutboundPort.fetchInventory(inventoryState.id)

        if (cacheFetchResult == RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode)
            throw InventoryException.NoCachedInventoryFound()

        inventoryState.setQuantity(cacheFetchResult)

        return inventoryState
    }

    override fun loadInventory(inventoryId: Long): InventoryDomainEntity {
        val inventoryState = inventoryStateQueryOutboundPort.fetch(inventoryId)

        val cacheFetchResult = inventoryCacheQueryOutboundPort.fetchInventory(inventoryId)

        if (cacheFetchResult == RedisConfig.InventoryScriptError.NO_CACHED_INVENTORY_FOUND.errorCode)
            throw InventoryException.NoCachedInventoryFound()

        inventoryState.setQuantity(cacheFetchResult)

        return inventoryState
    }
}