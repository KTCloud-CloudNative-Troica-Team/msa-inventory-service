package dev.ktcloud.black.inventory.event.application.port.outbound

import dev.ktcloud.black.inventory.event.domain.entity.InventoryEventDomainEntity

interface InventoryEventCommandOutboundPort {
    fun save(event: InventoryEventDomainEntity): InventoryEventDomainEntity
    fun saveAll(events: List<InventoryEventDomainEntity>)
}