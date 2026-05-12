package dev.ktcloud.black.inventory.event.application.service

import dev.ktcloud.black.inventory.event.application.port.inbound.CreateInventoryEventCommand
import dev.ktcloud.black.inventory.event.application.port.inbound.SetStatusProcessedCommand
import dev.ktcloud.black.inventory.event.application.port.outbound.InventoryEventCommandOutboundPort
import dev.ktcloud.black.inventory.event.application.port.outbound.InventoryEventQueryOutboundPort
import dev.ktcloud.black.inventory.event.domain.entity.InventoryEventDomainEntity
import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventProcessStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryEventCommandService(
    private val inventoryEventQueryOutboundPort: InventoryEventQueryOutboundPort,
    private val inventoryEventCommandOutboundPort: InventoryEventCommandOutboundPort,
): CreateInventoryEventCommand, SetStatusProcessedCommand {
    @Transactional
    override fun create(command: CreateInventoryEventCommand.In) {
        val inventoryEventDomainEntity = InventoryEventDomainEntity(
            inventoryId = command.inventoryId,
            amount = command.amount,
            eventType = command.eventType,
        )

        inventoryEventDomainEntity.checkDomainEntityValidity()

        inventoryEventCommandOutboundPort.save(inventoryEventDomainEntity)
    }

    @Transactional
    override fun setStatusProcessed(command: SetStatusProcessedCommand.In) {
        val inventoryEvents = inventoryEventQueryOutboundPort.fetchAll(command.ids)

        inventoryEvents.forEach {
            it.updateProcessStatus(InventoryEventProcessStatus.PROCESSED)
        }

        inventoryEventCommandOutboundPort.saveAll(inventoryEvents)
    }
}