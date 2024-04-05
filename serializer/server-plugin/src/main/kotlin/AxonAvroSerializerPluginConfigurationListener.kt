package io.holixon.axon.avro.serializer.plugin

import io.axoniq.axonserver.plugin.Configuration
import io.axoniq.axonserver.plugin.ConfigurationListener
import io.axoniq.axonserver.plugin.PluginPropertyDefinition
import mu.KLogging

/**
 * Allows configuration of plugin via [ConfigurationListener], see [configuring a plugin](https://docs.axoniq.io/reference-guide/v/master/axon-server/administration/plugins#configuring-a-plugin).
 */
class AxonAvroSerializerPluginConfigurationListener : ConfigurationListener, AxonAvroSerializerPluginPropertiesForContextResolver {

  companion object : KLogging() {
    /**
     * Default context.
     */
    const val DEFAULT_CONTEXT = "default"
  }

  private val propertiesPerContext: MutableMap<String, AxonAvroSerializerPluginProperties> = mutableMapOf()

  override fun configuration(): Configuration = Configuration(
    listOf(
      PluginPropertyDefinition
        .newBuilder(AxonAvroSerializerPluginProperties.KEY_URL_TEMPLATE, "Registry URL Template")
        .description("URL of the remote schema registry")
        .defaultValue(AxonAvroSerializerPluginProperties.DEFAULT_URL_TEMPLATE)
        .build()
    ),
    "Axon Avro Serializer Configuration"
  )

  override fun updated(context: String, configuration: Map<String, *>?) {
    val new = AxonAvroSerializerPluginProperties(configuration)
    val updated = propertiesPerContext.put(context, new)
    logger.debug { "Updated properties for context=$context, old configuration=$updated, new configuration=$new" }
  }

  override fun removed(context: String) {
    val removed = propertiesPerContext.remove(context)
    logger.debug { "Removed configuration for context=$context, configuration=$removed" }
  }

  override fun getAxonAvroProperties(contextName: String): AxonAvroSerializerPluginProperties {
    return propertiesPerContext[contextName] ?: if (contextName == DEFAULT_CONTEXT) {
      AxonAvroSerializerPluginProperties(mapOf())
    } else {
      throw IllegalArgumentException("context[$contextName] not configured")
    }
  }

  override fun toString(): String = "Context Configurations: $propertiesPerContext"
}

