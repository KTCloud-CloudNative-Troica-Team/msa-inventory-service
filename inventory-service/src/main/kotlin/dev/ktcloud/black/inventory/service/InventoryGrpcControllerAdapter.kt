package dev.ktcloud.black.inventory.service

import dev.ktcloud.black.inventory.application.port.inbound.command.CreateInventoryCommand
import dev.ktcloud.black.inventory.application.port.inbound.command.DecreaseInventoryCommand
import dev.ktcloud.black.inventory.application.port.inbound.command.IncreaseInventoryCommand
import dev.ktcloud.black.inventory.application.port.inbound.query.FetchInventoriesQuery
import dev.ktcloud.black.inventory.application.port.inbound.query.LoadInventoryQuery
import dev.ktcloud.black.inventory.domain.entity.InventoryDomainEntity
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.CreateInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.DecreaseInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.Empty
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.FetchInventoriesResponse
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.FetchInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.IncreaseInventoryRequest
import dev.ktcloud.black.inventory.service.adapter.presentation.web.inbound.grpc.InventoryResponseDto
import dev.ktcloud.black.inventory.service.adapter.web.inbound.InventoryGrpcController
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class InventoryGrpcControllerAdapter(
    private val createInventoryCommand: CreateInventoryCommand,
    private val decreaseInventoryCommand: DecreaseInventoryCommand,
    private val increaseInventoryCommand: IncreaseInventoryCommand,
    private val fetchInventoriesQuery: FetchInventoriesQuery,
    private val loadInventoryQuery: LoadInventoryQuery
): InventoryGrpcController() {
    private fun buildInventoryResponseDto(
        id: Long,
        productId: String,
        skuCode: String,
        quantity: Int
    ): InventoryResponseDto {
        return InventoryResponseDto.newBuilder()
            .setId(id)
            .setProductId(productId)
            .setSkuCode(skuCode)
            .setQuantity(quantity)
            .build()
    }

    override suspend fun createInventory(request: CreateInventoryRequest): InventoryResponseDto {
        val created = createInventoryCommand.create(
            CreateInventoryCommand.In(
                productId = request.productId,
                skuCode = request.skuCode,
            )
        )

        return buildInventoryResponseDto(
            id = created.id,
            productId = created.productId,
            skuCode = created.skuCode,
            quantity = created.quantity
        )
    }

    override suspend fun decreaseInventory(request: DecreaseInventoryRequest): InventoryResponseDto {
        val decreased = decreaseInventoryCommand.decrease(
            DecreaseInventoryCommand.In(
                orderId = request.orderId,
                inventoryId = request.inventoryId,
                amount = request.amount
            )
        )

        return buildInventoryResponseDto(
            id = decreased.id,
            productId = decreased.productId,
            skuCode = decreased.skuCode,
            quantity = decreased.quantity
        )
    }

    override suspend fun increaseInventory(request: IncreaseInventoryRequest): InventoryResponseDto {
        val increased = increaseInventoryCommand.increase(
            IncreaseInventoryCommand.In(
                inventoryId = request.inventoryId,
                amount = request.amount
            )
        )

        return buildInventoryResponseDto(
            id = increased.id,
            productId = increased.productId,
            skuCode = increased.skuCode,
            quantity = increased.quantity
        )
    }

    override suspend fun fetchInventory(request: FetchInventoryRequest): InventoryResponseDto {
        val inventory = loadInventoryQuery.load(
            LoadInventoryQuery.In(request.id)
        )

        return buildInventoryResponseDto(
            id = inventory.id,
            productId = inventory.productId,
            skuCode = inventory.skuCode,
            quantity = inventory.quantity
        )
    }

    override suspend fun fetchInventories(request: Empty): FetchInventoriesResponse {
        val inventories = fetchInventoriesQuery.fetchAll()

        val inventoryResponseDtos = inventories.map {
            buildInventoryResponseDto(
                id = it.id,
                productId = it.productId,
                skuCode = it.skuCode,
                quantity = it.quantity
            )
        }

        return FetchInventoriesResponse.newBuilder()
            .addAllInventories(inventoryResponseDtos)
            .build()
    }
}