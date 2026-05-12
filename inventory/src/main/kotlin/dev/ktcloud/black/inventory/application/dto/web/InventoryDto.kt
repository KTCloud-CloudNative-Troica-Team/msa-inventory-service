package dev.ktcloud.black.inventory.application.dto.web

data class InventoryDto(
    val id: Long,
    val productId: String,
    val skuCode: String,
    val quantity: Int,
)
