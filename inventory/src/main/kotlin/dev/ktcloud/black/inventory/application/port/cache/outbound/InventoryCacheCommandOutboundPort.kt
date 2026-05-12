package dev.ktcloud.black.inventory.application.port.cache.outbound

interface InventoryCacheCommandOutboundPort {
    fun decrease(inventoryId: Long, amount: Int, eventId: Long): Int
    fun increase(inventoryId: Long, amount: Int, eventId: Long): Int
    fun setInventoryQuantity(inventoryId: Long, quantity: Int, eventId: Long): Int
}