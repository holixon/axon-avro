package io.holixon.axon.avro.serializer.plugin

import java.net.URI

/**
 * Configuration properties for the Axon Avro Serializer Server plugin.
 */
data class AxonAvroSerializerPluginProperties(
  val registryUrlTemplate: String
) {
  companion object {

    /**
     * Empty properties.
     */
    val EMPTY = AxonAvroSerializerPluginProperties(
      registryUrlTemplate = ""
    )

    const val PARAM_FINGERPRINT = "{fingerprint}"

    /**
     * Property key for the registry URL.
     */
    const val KEY_URL_TEMPLATE = "registry-url-template"

    /**
     * Default registry URL template.
     */
    const val DEFAULT_URL_TEMPLATE = "http://localhost:9191/schema?fingerprint=${PARAM_FINGERPRINT}"

    /**
     * Factory methods to create properties out of map.
     */
    operator fun invoke(map: Map<String, Any?>? = null) = if (map != null) {
      AxonAvroSerializerPluginProperties(
        registryUrlTemplate = map.getOrDefault(KEY_URL_TEMPLATE, DEFAULT_URL_TEMPLATE) as String,
      )
    } else {
      EMPTY
    }
  }

  fun validate() {
    require(registryUrlTemplate.contains(PARAM_FINGERPRINT)) { "Registry URL template must contain param '${PARAM_FINGERPRINT}'" }
    // try to build the URL with example fingerprint
    buildRegistryUri(1L)
  }

  fun buildRegistryUri(fingerPrint: Long): URI = URI(registryUrlTemplate.replace(PARAM_FINGERPRINT, "$fingerPrint"))
}
