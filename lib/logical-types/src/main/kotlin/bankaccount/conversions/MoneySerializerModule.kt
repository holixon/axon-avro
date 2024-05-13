package bankaccount.conversions

import io.toolisticon.avro.kotlin.logicaltypes.spi.SerializersModuleFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import org.javamoney.moneta.Money

/**
 * A module definition with all custom serializers present in this Module. Registered via SPI.
 * To use it please add the module adding `SerializersModuleLoader.scanAndRegisterModules()`.
 */
@ExperimentalSerializationApi
class MoneySerializerModule : SerializersModuleFactory {

  override fun customModule(): SerializersModule {
    return SerializersModule {
      contextual(Money::class, MoneySerializer())
    }
  }
}
