package dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.InventoryEventMapper
import dev.ktcloud.black.inventory.event.application.port.outbound.InventoryEventCommandOutboundPort
import dev.ktcloud.black.inventory.event.domain.entity.InventoryEventDomainEntity
import org.springframework.stereotype.Component

@Component
class InventoryEventPostgresqlCommandRepository(
    private val repository: InventoryEventPostgresqlRepository,
    private val mapper: InventoryEventMapper
): InventoryEventCommandOutboundPort {
    override fun save(event: InventoryEventDomainEntity): InventoryEventDomainEntity {
        val ormEntity = mapper.toOrmEntity(event)

        val saved = repository.save(ormEntity)

        return mapper.toDomainEntity(saved)
    }

    override fun saveAll(events: List<InventoryEventDomainEntity>) {
        val ormEntities = events.map { mapper.toOrmEntity(it) }

        repository.saveAll(ormEntities)
    }
}