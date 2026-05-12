package dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.InventoryEventMapper
import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.entity.QInventoryEvent.inventoryEvent
import dev.ktcloud.black.inventory.event.application.port.outbound.InventoryEventQueryOutboundPort
import dev.ktcloud.black.inventory.event.domain.entity.InventoryEventDomainEntity
import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventProcessStatus
import org.springframework.stereotype.Component

@Component
class InventoryEventPostgresqlQueryRepository(
    private val repository: InventoryEventPostgresqlRepository,
    private val mapper: InventoryEventMapper,
    private val queryFactory: JPAQueryFactory
): InventoryEventQueryOutboundPort {
    override fun fetchUnprocessedEvents(inventoryId: Long): List<InventoryEventDomainEntity> {
        val qb = queryFactory.selectFrom(inventoryEvent)
            .where(
                inventoryEvent.inventoryId.eq(inventoryId),
                inventoryEvent.processStatus.eq(InventoryEventProcessStatus.PENDING)
            )
            .orderBy(inventoryEvent.id.asc())

        return mapper.toDomainEntity(qb.fetch())
    }

    override fun fetchAll(ids: List<Long>): List<InventoryEventDomainEntity> {
        val entities = repository.findAllById(ids)

        return mapper.toDomainEntity(entities.toList())
    }
}