package dev.ktcloud.black.inventory.domain.event

data class InventoryDecreased(
    val id: Long,
    val decreasedAmount: Int,
)
