package dev.ktcloud.black.inventory.service.worker.batch

import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.InventoryEventMapper
import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.entity.InventoryEvent
import dev.ktcloud.black.inventory.event.adapter.infrastructure.jpa.repository.InventoryEventPostgresqlRepository
import dev.ktcloud.black.inventory.event.application.port.inbound.SetStatusProcessedCommand
import dev.ktcloud.black.inventory.event.domain.entity.InventoryEventDomainEntity
import dev.ktcloud.black.inventory.event.domain.vo.InventoryEventProcessStatus
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.transaction.PlatformTransactionManager

/**
 * Q7 — InventoryEvent Event Sourcing projection batch job.
 *
 * `worker` 프로파일에서만 활성. inventory-service-worker K8s Deployment가 Job 실행 후 종료.
 *
 * 1. ItemReader: `InventoryEventProcessStatus.PENDING` 인 InventoryEvent 청크 fetch
 * 2. ItemProcessor: ORM → domain entity 변환 (projection 로직 확장 지점)
 * 3. ItemWriter: 청크 단위로 PROCESSED 마킹 + (추후) read model 갱신 / audit 적재
 *
 * 진짜 Event Sourcing의 가치는 read model을 이벤트로부터 재구성할 수 있다는 점.
 * 현재 InventoryEvent는 모든 mutation 이벤트를 append-only로 기록 중.
 * projection 로직만 추가하면 일별 통계 / audit / 외부 시스템 동기화 등 다양한 read model 가능.
 */
@Configuration
@Profile("worker")
class InventoryEventProjectionBatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val repository: InventoryEventPostgresqlRepository,
    private val mapper: InventoryEventMapper,
    private val setStatusProcessedCommand: SetStatusProcessedCommand,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun inventoryEventProjectionJob(): Job =
        JobBuilder("inventoryEventProjectionJob", jobRepository)
            .start(inventoryEventProjectionStep())
            .build()

    @Bean
    fun inventoryEventProjectionStep(): Step =
        StepBuilder("inventoryEventProjectionStep", jobRepository)
            .chunk<InventoryEvent, InventoryEventDomainEntity>(50, transactionManager)
            .reader(pendingInventoryEventReader())
            .processor(toDomainProcessor())
            .writer(projectionWriter())
            .build()

    /**
     * PoC 단순화: 전체 fetch 후 PENDING만 필터링. 실 운영 시 `JpaPagingItemReader` +
     * QueryDSL 페이징(`fetchUnprocessed()`)으로 교체 권장.
     */
    @Bean
    @JobScope
    fun pendingInventoryEventReader(): ItemReader<InventoryEvent> {
        val pending = repository.findAll()
            .filter { it.processStatus == InventoryEventProcessStatus.PENDING }
            .iterator()
        return ItemReader { if (pending.hasNext()) pending.next() else null }
    }

    @Bean
    @StepScope
    fun toDomainProcessor(): ItemProcessor<InventoryEvent, InventoryEventDomainEntity> =
        ItemProcessor { event -> mapper.toDomainEntity(event) }

    @Bean
    @StepScope
    fun projectionWriter(): ItemWriter<InventoryEventDomainEntity> =
        object : ItemWriter<InventoryEventDomainEntity> {
            override fun write(chunk: Chunk<out InventoryEventDomainEntity>) {
                val ids: List<Long> = chunk.items.map { it.id }
                log.info("Projecting {} inventory events: ids={}", ids.size, ids)
                // 실제 read model 생성 로직(일별 집계, audit 적재 등)은 본 메서드 내에 추가.
                setStatusProcessedCommand.setStatusProcessed(SetStatusProcessedCommand.In(ids))
            }
        }
}
