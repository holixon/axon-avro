package io.holixon.axon.avro.serializer

import io.toolisticon.kotlin.avro.value.JsonString
import org.axonframework.serialization.RevisionResolver

object AxonAvroKotlinSerializerHelper {

  val REVISION_RESOLVER_NULL: RevisionResolver = RevisionResolver { null }
  val REVISION_RESOLVER_BLANK: RevisionResolver = RevisionResolver { "" }

  object SchemaJson {
    val compatibleSchemaWithoutValue2: JsonString = JsonString.of(
      """
        {
          "name": "ComplexObject",
          "namespace": "io.holixon.axon.avro.serializer._test",
          "type": "record",
          "fields": [
            {
              "name": "value1",
              "type": "string"
            },
            {
              "name": "value3",
              "type": "int"
            }
          ]
       }
    """.trimIndent()
    )

    val incompatibleSchema = JsonString.of(
      """
      {
        "name": "ComplexObject",
        "namespace": "io.holixon.axon.avro.serializer._test",
        "type": "record",
        "fields": [
          {
            "name": "value2",
            "type": "string"
          },
          {
            "name": "value3",
            "type": "int"
          }
         ]
      }
    """.trimIndent()
    )

    val compatibleSchema = JsonString.of(
      """
      {
        "name": "ComplexObject",
        "namespace": "io.holixon.axon.avro.serializer._test",
        "type": "record",
        "fields": [
          {
            "name": "value1",
            "type": "string"
          },
          {
            "name": "value2",
            "type": "string"
          },
          {
            "name": "value3",
            "type": "int"
          }
        ]
      }
    """.trimIndent()
    )

    val compatibleSchemaWithAdditionalField = JsonString.of(
      """
      {
        "name": "ComplexObject",
        "namespace": "io.holixon.axon.avro.serializer._test",
        "type": "record",
        "fields": [
          {
            "name": "value1",
            "type": "string"
          },
          {
            "name": "value2",
            "type": "string"
          },
          {
            "name": "value4",
            "type": "string"
          },
          {
            "name": "value3",
            "type": "int"
          }
        ]
      }
    """.trimIndent()
    )
  }
}
