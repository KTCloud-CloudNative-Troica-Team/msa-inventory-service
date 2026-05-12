package dev.ktcloud.black.inventory.event.domain.entity

import dev.ktcloud.black.inventory.event.domain.exception.InventoryEventException
import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventProcessStatus
import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventType

data class InventoryEventDomainEntity(
    val id: Long = 0L,
    val inventoryId: Long,
    val amount: Int,
    val eventType: InventoryEventType,
    private var _processStatus: InventoryEventProcessStatus = InventoryEventProcessStatus.PENDING
) {
    val processStatus: InventoryEventProcessStatus
        get() = _processStatus

    fun checkDomainEntityValidity() {
        if (amount == 0) throw InventoryEventException.InventoryEventInvalid()
        if (eventType == InventoryEventType.DECREMENT && amount > 0) throw InventoryEventException.InventoryEventInvalid()
        if (eventType == InventoryEventType.INCREMENT && amount < 0) throw InventoryEventException.InventoryEventInvalid()
    }

    fun updateProcessStatus(status: InventoryEventProcessStatus) {
        _processStatus = status
    }
}