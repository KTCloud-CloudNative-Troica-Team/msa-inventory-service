package dev.ktcloud.black.inventory.event.application.port.outbound

import dev.ktcloud.black.inventory.event.domain.entity.InventoryEventDomainEntity

interface InventoryEventQueryOutboundPort {
    fun fetchUnprocessedEvents(inventoryId: Long): List<InventoryEventDomainEntity>
    fun fetchAll(ids: List<Long>): List<InventoryEventDomainEntity>
}