package dev.ktcloud.black.inventory.domain.entity

import dev.ktcloud.black.inventory.domain.exception.InventoryException

data class InventoryDomainEntity(
    val id: Long = 0L,
    val productId: String,
    val skuCode: String,
    private var _quantity: Int = 0
) {
    val quantity: Int
        get() = _quantity

    fun increaseQuantity(amount: Int) {
        _quantity += amount
    }

    fun decreaseQuantity(amount: Int) {
        if (_quantity < amount) throw InventoryException.InventoryNotEnough()

        _quantity -= amount
    }

    fun setQuantity(amount: Int) {
        _quantity = amount
    }
}