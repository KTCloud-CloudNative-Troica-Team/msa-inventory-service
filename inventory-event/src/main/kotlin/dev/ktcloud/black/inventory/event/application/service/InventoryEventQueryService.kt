package dev.ktcloud.black.inventory.event.application.service

import dev.ktcloud.black.inventory.event.application.port.inbound.FetchUnprocessedInventoryEventsQuery
import dev.ktcloud.black.inventory.event.application.port.outbound.InventoryEventQueryOutboundPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryEventQueryService(
    private val inventoryEventQueryOutboundPort: InventoryEventQueryOutboundPort
): FetchUnprocessedInventoryEventsQuery {
    @Transactional(readOnly = true)
    override fun fetchUnprocessed(query: FetchUnprocessedInventoryEventsQuery.In): List<FetchUnprocessedInventoryEventsQuery.Out> {
        val inventoryEventDomainEntities = inventoryEventQueryOutboundPort.fetchUnprocessedEvents(query.inventoryId)

        return inventoryEventDomainEntities.map {
            FetchUnprocessedInventoryEventsQuery.Out(
                id = it.id,
                inventoryId = it.inventoryId,
                amount = it.amount,
                eventType = it.eventType,
                processStatus = it.processStatus
            )
        }
    }
}