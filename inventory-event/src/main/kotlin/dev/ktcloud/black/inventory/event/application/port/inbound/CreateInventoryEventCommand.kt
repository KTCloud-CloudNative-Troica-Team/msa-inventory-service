package dev.ktcloud.black.inventory.event.application.port.inbound

import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventType

interface CreateInventoryEventCommand {
    fun create(command: In)

    data class In(
        val inventoryId: Long,
        val amount: Int,
        val eventType: InventoryEventType,
    )
}