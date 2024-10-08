package io.holixon.axon.avro.serializer.plugin

import io.toolisticon.kotlin.avro.codec.SingleObjectToJsonConverter


/**
 * Provider for SingleObjectToJsonConverter.
 */
interface SingleObjectToJsonConverterProvider {
  /**
   * Retrieves a converter for given context.
   * @param contextName name of the context.
   */
  fun get(contextName: String): SingleObjectToJsonConverter
}
