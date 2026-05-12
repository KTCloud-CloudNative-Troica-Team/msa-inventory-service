package dev.ktcloud.black.inventory.application.port.inbound.query

import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity

interface LoadInventoryQuery {
    fun load(query: In): Out

    data class In(
        val id: Long
    )

    data class Out(
        val id: Long,
        val productId: String,
        val skuCode: String,
        val quantity: Int
    ) {
        companion object {
            fun from(domainEntity: InventoryDomainEntity): Out {
                return Out(
                    id = domainEntity.id,
                    productId = domainEntity.productId,
                    skuCode = domainEntity.skuCode,
                    quantity = domainEntity.quantity
                )
            }
        }
    }
}