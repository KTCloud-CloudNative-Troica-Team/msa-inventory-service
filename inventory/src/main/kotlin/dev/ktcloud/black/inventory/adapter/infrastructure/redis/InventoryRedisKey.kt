package dev.ktcloud.black.inventory.adapter.infrastructure.redis

data class InventoryRedisKey(
    val inventoryId: Long
) {
    fun toRedisKey() = "inventory:$inventoryId"

    override fun toString(): String = toRedisKey()
}