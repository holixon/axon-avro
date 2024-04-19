package io.toolisticon.avro.kotlin.logicaltypes.spi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule

/**
 * Implement this interface and register the serializers.
 */
interface SerializersModuleFactory {

  @ExperimentalSerializationApi
  fun customModule(): SerializersModule
}
