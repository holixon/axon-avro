package io.holixon.axon.avro.serializer.spring

import io.toolisticon.kotlin.avro.repository.AvroSchemaResolver
import org.springframework.context.annotation.Import

/**
 * Annotation to switch the Axon Avro Serialization for events.
 * Will require [AvroSchemaResolver] bean to be available.
 */
@MustBeDocumented
@Import(value = [
  AxonAvroSerializerConfiguration::class,
  AvroSchemaScannerConfiguration::class
])
annotation class EnableAxonAvroSerializer
