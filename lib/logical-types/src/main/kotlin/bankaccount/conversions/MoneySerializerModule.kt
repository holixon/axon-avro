package bankaccount.conversions

import io.toolisticon.avro.kotlin.logicaltypes.spi.SerializersModuleFactory
import kotlinx.serialization.modules.SerializersModule
import org.javamoney.moneta.Money

/**
 * A module definition with all custom serializers.
 */
class MoneySerializerModule : SerializersModuleFactory {

  override fun customModule(): SerializersModule {
    return SerializersModule {
      contextual(Money::class, MoneySerializer())
    }
  }
}
