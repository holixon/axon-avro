package io.holixon.avro.lib.test.schema

import org.apache.avro.Schema

interface TestSchemaDataProvider {
  val schemaJson: String

  val schemaData: TestSchemaData
    get() = TestSchemaData(schemaJson)

  val schema: Schema
    get() = schemaData.schema

  fun toJson() = schema.toString(true)
}
