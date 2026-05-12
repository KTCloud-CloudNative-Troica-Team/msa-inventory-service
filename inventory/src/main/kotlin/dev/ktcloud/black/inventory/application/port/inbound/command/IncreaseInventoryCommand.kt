package dev.ktcloud.black.inventory.application.port.inbound.command

import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity

interface IncreaseInventoryCommand {
    fun increase(command: In): Out

    data class In(
        val inventoryId: Long,
        val amount: Int
    )

    data class Out(
        val id: Long,
        val productId: String,
        val skuCode: String,
        val quantity: Int,
    ) {
        companion object {
            fun from(inventory: InventoryDomainEntity): Out {
                return Out(
                    id = inventory.id,
                    productId = inventory.productId,
                    skuCode = inventory.skuCode,
                    quantity = inventory.quantity,
                )
            }
        }
    }
}