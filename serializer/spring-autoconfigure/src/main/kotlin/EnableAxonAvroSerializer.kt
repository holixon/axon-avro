package io.holixon.axon.avro.serializer.spring

import org.springframework.context.annotation.Import

/**
 * Annotation to switch the Axon Avro Serialization for events.
 * Will require [io.holixon.avro.adapter.api.AvroSchemaReadOnlyRegistry] bean to be available, usually provided by one of the Registry Adapters for more details.
 */
@MustBeDocumented
@Import(AxonAvroSerializerConfiguration::class)
annotation class EnableAxonAvroSerializer
