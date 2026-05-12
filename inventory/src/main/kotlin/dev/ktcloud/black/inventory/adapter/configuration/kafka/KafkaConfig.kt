package dev.ktcloud.black.inventory.adapter.configuration.kafka

import dev.ktcloud.black.inventory.adapter.infrastructure.kafka.model.InventoryReserveRequestMessage
import dev.ktcloud.black.inventory.adapter.infrastructure.kafka.model.InventoryReservedResultMessage
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {
    @Bean
    fun inventoryReservedResultKafkaTemplate(): KafkaTemplate<String, InventoryReservedResultMessage> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            JsonSerializer.ADD_TYPE_INFO_HEADERS to false,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to Int.MAX_VALUE,
            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to 120000,
        )
        return KafkaTemplate(DefaultKafkaProducerFactory(configProps))
    }

    @Bean
    fun inventoryReserveRequestConsumerFactory(): ConsumerFactory<String, InventoryReserveRequestMessage> {
        val deserializer = JsonDeserializer(InventoryReserveRequestMessage::class.java).apply {
            addTrustedPackages("dev.ktcloud.black.*")
            setUseTypeHeaders(false)
        }

        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )

        return DefaultKafkaConsumerFactory(configProps, StringDeserializer(), deserializer)
    }

    // Q5: container factory 이름을 토픽명 정렬에 맞게 inventoryReserveRequestContainerFactory
    //     → orderPendingContainerFactory 로 변경. Listener의 @KafkaListener(containerFactory)도 일치.
    @Bean
    fun orderPendingContainerFactory(
        configurer: ConcurrentKafkaListenerContainerFactoryConfigurer
    ): ConcurrentKafkaListenerContainerFactory<String, InventoryReserveRequestMessage> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, InventoryReserveRequestMessage>()
        val cf = inventoryReserveRequestConsumerFactory()

        factory.consumerFactory = cf

        configurer.configure(
            factory as ConcurrentKafkaListenerContainerFactory<Any, Any>,
            cf as ConsumerFactory<Any, Any>
        )

        return factory
    }
}