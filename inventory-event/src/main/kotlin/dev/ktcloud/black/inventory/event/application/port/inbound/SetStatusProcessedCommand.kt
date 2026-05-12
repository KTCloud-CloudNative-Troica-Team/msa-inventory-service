package dev.ktcloud.black.inventory.event.application.port.inbound

interface SetStatusProcessedCommand {
    fun setStatusProcessed(command: In)

    data class In(
        val ids: List<Long>
    )
}