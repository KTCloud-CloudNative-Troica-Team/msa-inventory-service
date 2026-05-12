package dev.ktcloud.black.inventory.event.domain.exception

import dev.ktcloud.black.common.exception.CustomException
import org.springframework.http.HttpStatus

class InventoryEventException {
    class InventoryEventInvalid(message: String? = null, e: Throwable? = null): CustomException(
        "001",
        message ?: "無効な在庫イベントです",
        HttpStatus.BAD_REQUEST,
        e
    )
}