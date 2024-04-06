package io.holixon.axon.avro.serializer.spring

import com.github.avrokotlin.avro4k.Avro
import io.holixon.axon.avro.serializer.AvroSerializer
import io.toolisticon.avro.kotlin.AvroSchemaResolver
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
  }

  /**
   * Bean factory for the serializer builder.
   */
  @Bean
  @ConditionalOnMissingBean(AvroSerializer.Builder::class)
  fun defaultAxonSerializerBuilder(schemaResolver: AvroSchemaResolver): AvroSerializer.Builder = AvroSerializer.builder()
    .avroSchemaResolver(schemaResolver)
    .avro4k(Avro.default) // TODO: use correct setup with registered serializers


  /**
   * Bean factory for the serializer.
   */
//  @Bean
//  @Qualifier(EVENT_SERIALIZER)
//  fun avroSerializer(builder: AvroSerializer.Builder): Serializer = AvroSerializer(builder)


  @Bean
  @ConditionalOnProperty(value = ["\${axon.avro.serializer.rest-enabled}"], havingValue = "true", matchIfMissing = true)
  fun schemaResolverRestResource(schemaResolver: AvroSchemaResolver) = AvroSchemaResolverResource(schemaResolver = schemaResolver)
}
