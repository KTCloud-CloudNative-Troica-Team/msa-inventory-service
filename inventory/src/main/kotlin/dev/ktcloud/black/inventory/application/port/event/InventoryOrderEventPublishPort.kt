package dev.ktcloud.black.inventory.application.port.event

import dev.ktcloud.black.inventory.application.dto.event.outbound.InventoryReservedResultEvent

interface InventoryOrderEventPublishPort {
    fun publish(event: InventoryReservedResultEvent)
}