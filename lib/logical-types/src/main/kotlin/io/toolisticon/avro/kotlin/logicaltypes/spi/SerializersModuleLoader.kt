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
    val moduleFactories = ServiceLoader.load(SerializersModuleFactory::class.java).iterator().asSequence().toList()
    return if (moduleFactories.isNotEmpty()) {
      var module = moduleFactories.first().customModule()
      moduleFactories.subList(1, moduleFactories.size).forEach {
        module = module.plus(it.customModule())
      }
      module
    } else {
      EmptySerializersModule()
    }
  }
}
