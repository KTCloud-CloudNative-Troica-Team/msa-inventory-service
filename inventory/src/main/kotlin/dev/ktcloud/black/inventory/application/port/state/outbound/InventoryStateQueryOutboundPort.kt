package dev.ktcloud.black.inventory.application.port.state.outbound

import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity

interface InventoryStateQueryOutboundPort {
    fun fetch(productId: String, skuCode: String): InventoryDomainEntity
    fun fetch(id: Long): InventoryDomainEntity

    fun fetchAll(): List<InventoryDomainEntity>
    fun fetchInventories(ids: List<Long>): List<InventoryDomainEntity>
}