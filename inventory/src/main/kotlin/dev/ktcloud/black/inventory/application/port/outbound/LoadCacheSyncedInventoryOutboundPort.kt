package dev.ktcloud.black.inventory.application.port.outbound

import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity

interface LoadCacheSyncedInventoryOutboundPort {
    fun loadCacheSyncedInventory(productId: String, skuCode: String): InventoryDomainEntity
    fun loadCacheSyncedInventory(inventoryId: Long): InventoryDomainEntity
    fun loadInventory(productId: String, skuCode: String): InventoryDomainEntity
    fun loadInventory(inventoryId: Long): InventoryDomainEntity
}