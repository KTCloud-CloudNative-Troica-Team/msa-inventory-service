package dev.ktcloud.black.inventory.application.dto.event.outbound

import dev.ktcloud.black.inventory.domain.vo.InventoryReserveResultState

data class InventoryReservedResultEvent(
    val orderId: Long,
    val inventoryId: Long,
    val amount: Int,
    val resultState: InventoryReserveResultState
)