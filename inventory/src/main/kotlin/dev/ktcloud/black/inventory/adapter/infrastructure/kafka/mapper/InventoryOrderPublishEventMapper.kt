package dev.ktcloud.black.inventory.adapter.infrastructure.kafka.mapper

import dev.ktcloud.black.common.adapter.infrastructure.kafka.EventMapper
import dev.ktcloud.black.inventory.adapter.infrastructure.kafka.model.InventoryReservedResultMessage
import dev.ktcloud.black.inventory.application.dto.event.outbound.InventoryReservedResultEvent
import org.springframework.stereotype.Component

@Component
class InventoryOrderPublishEventMapper: EventMapper<InventoryReservedResultMessage, InventoryReservedResultEvent> {
    override fun toMessage(event: InventoryReservedResultEvent): InventoryReservedResultMessage {
        return InventoryReservedResultMessage(
            event.orderId,
            event.inventoryId,
            event.amount,
            event.resultState
        )
    }

    override fun toEvent(message: InventoryReservedResultMessage): InventoryReservedResultEvent {
        return InventoryReservedResultEvent(
            message.orderId,
            message.inventoryId,
            message.amount,
            message.resultState
        )
    }
}