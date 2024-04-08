package io.holixon.axon.avro.registry.plugin.properties

import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginProperties
import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginProperties.Companion.DEFAULT_URL_TEMPLATE
import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginProperties.Companion.EMPTY
import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginProperties.Companion.KEY_URL_TEMPLATE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AxonAvroSerializerPluginPropertiesTest {
  companion object {
    fun createProperties(registryTemplateUrl: String) = mapOf(
      KEY_URL_TEMPLATE to registryTemplateUrl,
    )
  }

  @Test
  internal fun `configure by map`() {
    val map = createProperties("http://localhost:9191/schema?fingerPrint={fingerprint}")
    val properties = AxonAvroSerializerPluginProperties(map)
    assertThat(properties.registryUrlTemplate).isEqualTo("http://localhost:9191/schema?fingerPrint={fingerprint}")
  }

  @Test
  internal fun `empty when map is null`() {
    assertThat(AxonAvroSerializerPluginProperties(null)).isEqualTo(EMPTY)
  }

  @Test
  internal fun `default values when map is empty`() {
    val properties = AxonAvroSerializerPluginProperties(emptyMap())
    assertThat(properties).isEqualTo(
      AxonAvroSerializerPluginProperties(
        registryUrlTemplate = DEFAULT_URL_TEMPLATE,
      )
    )
  }

  @Test
  internal fun `validates wrong url with missing fingerprint parameter`() {
    val e = assertThrows<IllegalArgumentException> {
      AxonAvroSerializerPluginProperties(
        createProperties("missing-scheme-url")
      )
    }
    assertThat(e.message).isEqualTo("Registry URL template must contain param '{fingerprint}'")
  }
}
