package dev.ktcloud.black.inventory.adapter.infrastructure.kafka

import dev.ktcloud.black.inventory.application.dto.event.inbound.InventoryReserveRequestEvent
import dev.ktcloud.black.inventory.application.port.event.InventoryOrderEventListenerPort
import dev.ktcloud.black.inventory.application.port.inbound.command.DecreaseInventoryCommand
import dev.ktcloud.black.inventory.application.service.InventoryCommandService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class InventoryOrderEventKafkaListener(
    private val inventoryCommandService: InventoryCommandService,
): InventoryOrderEventListenerPort {
    // Q5: inventory-reserve-request → order-pending (SPEC 표준 `order.pending`)
    @KafkaListener(
        topics = ["\${spring.kafka.topic.order-pending}"],
        groupId = "inventory-service-group",
        containerFactory = "orderPendingContainerFactory"
    )
    override fun onReserveRequest(event: InventoryReserveRequestEvent) {
        inventoryCommandService.decrease(
            DecreaseInventoryCommand.In(
                orderId = event.orderId,
                inventoryId = event.inventoryId,
                amount = event.amount
            )
        )
    }
}