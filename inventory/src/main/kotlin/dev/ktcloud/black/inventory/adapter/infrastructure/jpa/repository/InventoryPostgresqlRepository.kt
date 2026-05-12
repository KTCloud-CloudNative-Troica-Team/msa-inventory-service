package dev.ktcloud.black.inventory.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.inventory.adapter.infrastructure.jpa.entity.Inventory
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface InventoryPostgresqlRepository: CrudRepository<Inventory, Long> {
    fun findByProductIdAndSkuCode(productId: String, skuCode: String): Optional<Inventory>
}