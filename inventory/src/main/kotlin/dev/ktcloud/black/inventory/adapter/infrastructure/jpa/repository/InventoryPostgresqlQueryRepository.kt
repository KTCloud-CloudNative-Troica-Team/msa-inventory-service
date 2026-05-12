package dev.ktcloud.black.inventory.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.inventory.adapter.infrastructure.jpa.InventoryMapper
import dev.ktcloud.black.inventory.application.port.state.outbound.InventoryStateQueryOutboundPort
import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity
import dev.ktcloud.black.inventory.domain.exception.InventoryException
import org.springframework.stereotype.Component

@Component
class InventoryPostgresqlQueryRepository(
    private val repository: InventoryPostgresqlRepository,
    private val inventoryMapper: InventoryMapper
): InventoryStateQueryOutboundPort {
    override fun fetch(
        productId: String,
        skuCode: String
    ): InventoryDomainEntity {
        val optional = repository.findByProductIdAndSkuCode(productId, skuCode)

        if (!optional.isPresent) throw InventoryException.NoSuchInventory()

        return inventoryMapper.toDomainEntity(optional.get())
    }

    override fun fetch(id: Long): InventoryDomainEntity {
        val optional = repository.findById(id)

        if (!optional.isPresent) throw InventoryException.NoSuchInventory()

        return inventoryMapper.toDomainEntity(optional.get())
    }

    override fun fetchAll(): List<InventoryDomainEntity> {
        val all = repository.findAll()

        return inventoryMapper.toDomainEntity(all)
    }

    override fun fetchInventories(ids: List<Long>): List<InventoryDomainEntity> {
        val inventories = repository.findAllById(ids)

        return inventories.map(inventoryMapper::toDomainEntity)
    }
}