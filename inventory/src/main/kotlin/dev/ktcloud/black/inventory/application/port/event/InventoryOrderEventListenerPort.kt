package dev.ktcloud.black.inventory.application.port.event

import dev.ktcloud.black.inventory.application.dto.event.inbound.InventoryReserveRequestEvent

interface InventoryOrderEventListenerPort {
    fun onReserveRequest(event: InventoryReserveRequestEvent)
}