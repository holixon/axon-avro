package io.toolisticon.avro.kotlin.logicaltypes.spi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import java.util.*

object SerializersModuleLoader {

  @JvmStatic
  @OptIn(ExperimentalSerializationApi::class)
  fun scanAndRegisterModules(): SerializersModule {
    return ServiceLoader.load(SerializersModuleFactory::class.java).fold(EmptySerializersModule()) { acc,cur ->
      acc + cur.customModule()
    }
  }
}
