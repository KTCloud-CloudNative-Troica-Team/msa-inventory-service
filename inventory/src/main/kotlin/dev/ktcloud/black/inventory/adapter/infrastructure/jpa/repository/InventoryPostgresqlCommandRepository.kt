package dev.ktcloud.black.inventory.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.inventory.adapter.infrastructure.jpa.InventoryMapper
import dev.ktcloud.black.inventory.application.port.state.outbound.InventoryStateCommandOutboundPort
import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity
import org.springframework.stereotype.Component

@Component
class InventoryPostgresqlCommandRepository(
    private val repository: InventoryPostgresqlRepository,
    private val inventoryMapper: InventoryMapper
): InventoryStateCommandOutboundPort {
    override fun save(inventory: InventoryDomainEntity): InventoryDomainEntity {
        val inventoryOrmEntity = inventoryMapper.toOrmEntity(inventory)

        val saved = repository.save(inventoryOrmEntity)

        return inventoryMapper.toDomainEntity(saved)
    }
}