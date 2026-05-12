package dev.ktcloud.black.inventory.application.port.state.outbound

import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity

interface InventoryStateCommandOutboundPort {
    fun save(inventory: InventoryDomainEntity): InventoryDomainEntity
}