package dev.ktcloud.black.inventory.adapter.infrastructure.kafka.mapper

import dev.ktcloud.black.common.adapter.infrastructure.kafka.EventMapper
import dev.ktcloud.black.inventory.adapter.infrastructure.kafka.model.InventoryReserveRequestMessage
import dev.ktcloud.black.inventory.application.dto.event.inbound.InventoryReserveRequestEvent
import org.springframework.stereotype.Component

@Component
class InventoryOrderListenerEventMapper: EventMapper<InventoryReserveRequestMessage, InventoryReserveRequestEvent> {
    override fun toMessage(event: InventoryReserveRequestEvent): InventoryReserveRequestMessage {
        return InventoryReserveRequestMessage(
            event.orderId,
            event.inventoryId,
            event.amount
        )
    }

    override fun toEvent(message: InventoryReserveRequestMessage): InventoryReserveRequestEvent {
        return InventoryReserveRequestEvent(
            message.orderId,
            message.inventoryId,
            message.amount
        )
    }
}