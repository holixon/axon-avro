package io.holixon.axon.avro.serializer.spring

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties.
 */
@ConfigurationProperties(prefix = "axon.avro.serializer")
data class AxonAvroSerializerProperties(
  /**
   * Describes the base path for the REST controller exposing the schema registry.
   */
  val restBasePath: String = DEFAULT_REST_BASE_PATH,
  /**
   * Enable the rest interface.
   */
  val restEnabled: Boolean = false
) {
  companion object {
    const val DEFAULT_REST_BASE_PATH = "/rest/schema"
  }
}
