package io.holixon.axon.avro.serializer.plugin

/**
 * Resolver for configuration properties per context.
 */
interface AxonAvroSerializerPluginPropertiesForContextResolver {
  /**
   * Retrieves properties for context.
   * @param contextName name of Axon Server context.
   * @return properties.
   */
  fun getAxonAvroProperties(contextName: String): AxonAvroSerializerPluginProperties
}
