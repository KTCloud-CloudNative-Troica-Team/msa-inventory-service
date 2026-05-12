package dev.ktcloud.black.inventory.domain.vo

data class InventoryDecreaseIdempotencyKey(
    val id: Long,
    val orderId: Long
) {
    fun toIdempotencyKey() = "$id:$orderId"

    override fun toString(): String = toIdempotencyKey()
}
