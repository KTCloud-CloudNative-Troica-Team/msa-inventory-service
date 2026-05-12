package dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa

import dev.ktcloud.black.common.adapter.infrastructure.jpa.EntityMapper
import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.entity.InventoryEvent
import dev.ktcloud.black.inventory.event.domain.entity.InventoryEventDomainEntity
import org.springframework.stereotype.Component

@Component
class InventoryEventMapper: EntityMapper<InventoryEvent, InventoryEventDomainEntity> {
    override fun toOrmEntity(domainEntity: InventoryEventDomainEntity): InventoryEvent {
        return InventoryEvent(
            id = domainEntity.id,
            inventoryId = domainEntity.inventoryId,
            amount = domainEntity.amount,
            eventType = domainEntity.eventType,
            processStatus = domainEntity.processStatus,
        )
    }

    override fun toDomainEntity(entity: InventoryEvent): InventoryEventDomainEntity {
        return InventoryEventDomainEntity(
            id = entity.id,
            inventoryId = entity.inventoryId,
            amount = entity.amount,
            eventType = entity.eventType,
            _processStatus = entity.processStatus,
        )
    }
}