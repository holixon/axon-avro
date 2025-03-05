package io.holixon.axon.avro.serializer._test

import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import kotlinx.serialization.Serializable
import org.apache.avro.SchemaBuilder

@Serializable
data class BarString(val name: String)

val barStringSchema = AvroSchema(
  SchemaBuilder.record("BarString")
  .namespace("io.holixon.axon.avro.serializer._test")
  .fields()
  .requiredString("name")
  .endRecord())

