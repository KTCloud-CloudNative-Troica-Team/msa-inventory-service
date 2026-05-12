package dev.ktcloud.black.inventory.application.dto.event.inbound

data class InventoryReserveRequestEvent(
    val orderId: Long,
    val inventoryId: Long,
    val amount: Int
)