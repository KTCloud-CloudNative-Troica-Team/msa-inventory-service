package dev.ktcloud.black.inventory.adapter.infrastructure.kafka

import dev.ktcloud.black.inventory.adapter.infrastructure.kafka.mapper.InventoryOrderPublishEventMapper
import dev.ktcloud.black.inventory.adapter.infrastructure.kafka.model.InventoryReservedResultMessage
import dev.ktcloud.black.inventory.application.dto.event.outbound.InventoryReservedResultEvent
import dev.ktcloud.black.inventory.application.port.event.InventoryOrderEventPublishPort
import kotlinx.coroutines.future.await
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class InventoryOrderEventKafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, InventoryReservedResultMessage>,
    private val mapper: InventoryOrderPublishEventMapper,
    // Q5: inventory-reserved-result → order-inventory-reserved (SPEC 표준 `order.inventory-reserved`)
    @Value("\${spring.kafka.topic.order-inventory-reserved}")
    private val topicName: String
): InventoryOrderEventPublishPort {
    override fun publish(event: InventoryReservedResultEvent) {
        val message = mapper.toMessage(event)

        val future = kafkaTemplate.send(topicName, message.orderId.toString(), message)
    }
}