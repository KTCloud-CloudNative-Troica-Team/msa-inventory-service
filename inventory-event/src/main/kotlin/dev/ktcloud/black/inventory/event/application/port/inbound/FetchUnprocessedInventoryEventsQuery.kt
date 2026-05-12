package dev.ktcloud.black.inventory.event.application.port.inbound

import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventProcessStatus
import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventType

interface FetchUnprocessedInventoryEventsQuery {
    fun fetchUnprocessed(query: In): List<Out>

    data class In(
        val inventoryId: Long,
    )

    data class Out(
        val id: Long,
        val inventoryId: Long,
        val amount: Int,
        val eventType: InventoryEventType,
        val processStatus: InventoryEventProcessStatus
    )
}