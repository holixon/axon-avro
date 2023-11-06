package io.holixon.avro.adapter.registry.axon.itest.core

import holixon.registry.event.AvroSchemaRegisteredEvent
import io.holixon.avro.adapter.registry.axon.api.RegisterAvroSchemaCommand
import io.holixon.avro.adapter.registry.axon.core.AvroSchemaAggregate
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test

internal class AvroSchemaAggregateTest {
  val fixture = AggregateTestFixture(AvroSchemaAggregate::class.java)

  @Test
  fun `should register schema`() {
    val command = RegisterAvroSchemaCommand(
      schema = "schema",
      schemaId = "4711",
      name = "MyName",
      namespace = "my.namespace",
      revision = null
    )
    fixture
      .givenNoPriorActivity()
      .`when`(
        command
      ).expectEvents(
        AvroSchemaRegisteredEvent(
          command.schemaId,
          command.namespace,
          command.name,
          command.revision,
          command.schema
        )
      )
  }
}
