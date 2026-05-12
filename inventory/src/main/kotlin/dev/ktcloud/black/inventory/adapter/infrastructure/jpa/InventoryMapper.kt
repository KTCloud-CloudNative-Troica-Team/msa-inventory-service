package dev.ktcloud.black.inventory.adapter.infrastructure.jpa

import dev.ktcloud.black.common.adapter.infrastructure.jpa.EntityMapper
import dev.ktcloud.black.inventory.adapter.infrastructure.jpa.entity.Inventory
import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity
import org.springframework.stereotype.Component

@Component
class InventoryMapper: EntityMapper<Inventory, InventoryDomainEntity> {
    override fun toOrmEntity(domainEntity: InventoryDomainEntity): Inventory {
        return Inventory(
            id = domainEntity.id,
            productId = domainEntity.productId,
            skuCode = domainEntity.skuCode,
            quantity = domainEntity.quantity,
        )
    }

    override fun toDomainEntity(entity: Inventory): InventoryDomainEntity {
        return InventoryDomainEntity(
            id = entity.id,
            productId = entity.productId,
            skuCode = entity.skuCode,
            _quantity = entity.quantity,
        )
    }
}