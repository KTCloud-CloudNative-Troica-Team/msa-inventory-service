package dev.ktcloud.black.inventory.adapter.infrastructure.kafka.model

import dev.ktcloud.black.inventory.domain.vo.InventoryReserveResultState

data class InventoryReservedResultMessage(
    val orderId: Long,
    val inventoryId: Long,
    val amount: Int,
    val resultState: InventoryReserveResultState
)
