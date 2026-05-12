package dev.ktcloud.black.inventory.service.adapter.web.inbound

import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.CreateInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.DecreaseInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.Empty
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.FetchInventoriesResponse
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.FetchInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.IncreaseInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.InventoryResponseDto
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.InventoryServiceGrpcKt

abstract class InventoryGrpcController: InventoryServiceGrpcKt.InventoryServiceCoroutineImplBase() {
    abstract override suspend fun createInventory(request: CreateInventoryRequest): InventoryResponseDto

    abstract override suspend fun decreaseInventory(request: DecreaseInventoryRequest): InventoryResponseDto

    abstract override suspend fun increaseInventory(request: IncreaseInventoryRequest): InventoryResponseDto

    abstract override suspend fun fetchInventory(request: FetchInventoryRequest): InventoryResponseDto

    abstract override suspend fun fetchInventories(request: Empty): FetchInventoriesResponse
}