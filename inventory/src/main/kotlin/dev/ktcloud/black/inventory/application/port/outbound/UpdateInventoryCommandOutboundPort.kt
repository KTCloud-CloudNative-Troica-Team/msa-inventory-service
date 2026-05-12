package dev.ktcloud.black.inventory.application.port.outbound

interface UpdateInventoryCommandOutboundPort {
    fun increase(inventoryId: Long, amount: Int): Int
    fun decrease(inventoryId: Long, amount: Int): Int
}