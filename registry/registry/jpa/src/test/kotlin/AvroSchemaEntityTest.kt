package io.holixon.avro.adapter.registry.jpa

import io.holixon.avro.adapter.common.ext.DefaultSchemaExt.avroSchemaId
import io.holixon.avro.adapter.common.ext.DefaultSchemaExt.avroSchemaRevision
import io.holixon.avro.lib.test.AvroAdapterTestLib
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AvroSchemaEntityTest {
  private val entity = AvroSchemaEntity(
    schemaId = AvroAdapterTestLib.schemaSampleEvent4711.avroSchemaId,
    namespace = AvroAdapterTestLib.schemaSampleEvent4711.namespace,
    name = AvroAdapterTestLib.schemaSampleEvent4711.name,
    revision = AvroAdapterTestLib.schemaSampleEvent4711.avroSchemaRevision,
    description = AvroAdapterTestLib.schemaSampleEvent4711.doc,
    schema = AvroAdapterTestLib.loadArvoResource("test.fixture.SampleEvent-v4711")
  )

  @Test
  fun `should convert to DTO`() {
    val dto = entity.toDto()
    assertThat(dto.schemaId).isEqualTo(entity.schemaId)
    assertThat(dto.name).isEqualTo(entity.name)
    assertThat(dto.namespace).isEqualTo(entity.namespace)
    assertThat(dto.revision).isEqualTo(entity.revision)
  }
}
