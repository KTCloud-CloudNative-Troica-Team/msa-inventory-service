package dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.entity.InventoryEvent
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface InventoryEventPostgresqlRepository: CrudRepository<InventoryEvent, Long> {
}