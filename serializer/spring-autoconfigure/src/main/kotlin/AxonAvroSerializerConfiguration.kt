package io.holixon.axon.avro.serializer.spring

import io.holixon.axon.avro.serializer.AvroSerializer
import io.toolisticon.kotlin.avro.repository.AvroSchemaResolver
import io.toolisticon.kotlin.avro.repository.AvroSchemaResolverMap
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Configuration for Axon serializer.
 */
@EnableConfigurationProperties(AxonAvroSerializerProperties::class)
open class AxonAvroSerializerConfiguration {
  companion object {
    const val EVENT_SERIALIZER = "eventSerializer"
    const val MESSAGE_SERIALIZER = "messageSerializer"
    const val DEFAULT_SERIALIZER = "defaultSerializer"
  }

  @Bean
  @ConditionalOnMissingBean(AvroKotlinSerialization::class)
  fun avroKotlinSerialization(schemaResolver: AvroSchemaResolverMap): AvroKotlinSerialization {
    // TODO: use correct setup with registered serializers
    val avro = AvroKotlinSerialization()
    schemaResolver.values.forEach(avro::registerSchema)

    return avro
  }

  /**
   * Bean factory for the serializer builder.
   */
  @Bean
  @ConditionalOnMissingBean(AvroSerializer.Builder::class)
  fun defaultAxonSerializerBuilder(avro: AvroKotlinSerialization): AvroSerializer.Builder {

    return AvroSerializer
      .builder()
      .avroKotlinSerialization(avro)
  }

  /**
   * Bean factory for the serializer.
   */
//  @Bean
//  @Qualifier(EVENT_SERIALIZER)
//  fun avroSerializer(builder: AvroSerializer.Builder): Serializer = AvroSerializer(builder)


  @Bean
  @ConditionalOnProperty(value = ["\${axon.avro.serializer.rest-enabled}"], havingValue = "true", matchIfMissing = true)
  fun schemaResolverRestResource(avro: AvroKotlinSerialization) = AvroSchemaResolverResource(schemaResolver = avro)
}
