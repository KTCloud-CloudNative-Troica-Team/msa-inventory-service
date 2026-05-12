package dev.ktcloud.black.inventory.application.port.cache.outbound

interface InventoryCacheQueryOutboundPort {
    fun fetchInventory(inventoryId: Long): Int
}