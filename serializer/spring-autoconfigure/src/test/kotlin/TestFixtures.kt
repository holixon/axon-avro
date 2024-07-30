package io.holixon.axon.avro.serializer.spring

import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.value.JsonString
import upcaster.itest.DummyEvent

object TestFixtures {
  object DummyEvents {
    val jsonSchema01 = JsonString.of(
      """
      {
        "type": "record",
        "namespace": "upcaster.itest",
        "name": "DummyEvent",
        "revision": "1",
        "fields": [
          {
            "name": "value01",
            "type": {
              "type": "string",
              "avro.java.string": "String"
            }
          }
        ]
      }
    """.trimIndent()
    )

    val SCHEMA_EVENT_01: AvroSchema = AvroSchema.of(jsonSchema01)

    val SCHEMA_EVENT_10: AvroSchema = AvroSchema(DummyEvent.getClassSchema())

    val registry = AvroKotlin.avroSchemaResolver(listOf(SCHEMA_EVENT_01, SCHEMA_EVENT_10))

  }
}
