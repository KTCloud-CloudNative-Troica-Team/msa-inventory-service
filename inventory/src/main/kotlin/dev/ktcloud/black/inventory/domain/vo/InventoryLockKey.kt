package dev.ktcloud.black.inventory.domain.vo

data class InventoryLockKey(
    val inventoryId: Long
) {
    fun toLockKey() = "inventory-distributed-lock:$inventoryId"

    override fun toString(): String = toLockKey()
}
