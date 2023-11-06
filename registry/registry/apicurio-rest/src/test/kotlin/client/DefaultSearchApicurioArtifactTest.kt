package io.holixon.avro.adapter.registry.apicurio.client

import io.holixon.avro.adapter.api.AvroSchemaName
import io.holixon.avro.adapter.registry.apicurio.ArtifactId
import io.holixon.avro.adapter.registry.apicurio.GroupId
import io.holixon.avro.adapter.registry.apicurio.Version
import io.holixon.avro.adapter.registry.apicurio.type.ApicurioSearchedArtifactData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import java.lang.IllegalArgumentException

class DefaultSearchApicurioArtifactTest {

  private val defaultSearchApicurioArtifact = DefaultSearchApicurioArtifact(
    registryClient = mock(),
    schemaRevisionResolver = mock()
  )

  @Test
  fun `should fail if schema id is not provided`() {
    val thrown = assertThrows<IllegalArgumentException> {
      defaultSearchApicurioArtifact.findSchemaBySearchedArtifact(
        ApicurioSearchedArtifactData(
          1L,
          1L,
          GroupId(),
          ArtifactId(),
          AvroSchemaName(),
          Version(),
          null,
          mapOf(),
          listOf()
        )
      )
    }
    assertThat(thrown).hasMessage("Schema Id must not be null")
  }
}
