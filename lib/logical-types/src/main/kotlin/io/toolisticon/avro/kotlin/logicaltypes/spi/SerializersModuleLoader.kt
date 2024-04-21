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

    // much simpler: fold
//    val moduleFactories = ServiceLoader.load(SerializersModuleFactory::class.java).iterator().asSequence().toList()
//     if (moduleFactories.isNotEmpty()) {
//      var module = moduleFactories.first().customModule()
//      moduleFactories.subList(1, moduleFactories.size).forEach {
//        module = module.plus(it.customModule())
//      }
//      module
//    } else {
//      EmptySerializersModule()
//    }

    return ServiceLoader.load(SerializersModuleFactory::class.java).fold(EmptySerializersModule()) { acc,cur ->
      acc + cur.customModule()
    }
  }
}
