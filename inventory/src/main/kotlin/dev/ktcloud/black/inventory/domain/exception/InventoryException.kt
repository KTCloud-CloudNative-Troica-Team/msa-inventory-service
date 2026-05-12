package dev.ktcloud.black.inventory.domain.exception

import dev.ktcloud.black.common.exception.CustomException
import org.springframework.http.HttpStatus

sealed class InventoryException {
    class InventoryNotEnough(message: String? = null, e: Throwable? = null): CustomException(
        "001",
        message ?: "在庫を減らせません",
        HttpStatus.BAD_REQUEST,
        e
    )

    class NoSuchInventory(message: String? = null, e: Throwable? = null): CustomException(
        "002",
        message ?: "当在庫は存在しません",
        HttpStatus.NOT_FOUND,
        e
    )

    class NoCachedInventoryFound(message: String? = null, e: Throwable? = null): CustomException(
        "003",
        message ?: "在庫キャッシュは存在しません",
        HttpStatus.NOT_FOUND,
        e
    )

    class InventoryDataStale(message: String? = null, e: Throwable? = null): CustomException(
        "004",
        message ?: "在庫情報が古いんです",
        HttpStatus.BAD_REQUEST,
        e
    )

    class InventoryCacheUnreadable(message: String? = null, e: Throwable? = null): CustomException(
        "005",
        message ?: "在庫のキャッシュが読めない状態です",
        HttpStatus.BAD_REQUEST,
        e
    )
}