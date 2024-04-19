package bankaccount.conversions

import io.toolisticon.avro.kotlin.logicaltypes.GeneralizedSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import org.javamoney.moneta.Money

@OptIn(ExperimentalSerializationApi::class)
class MoneySerializer : GeneralizedSerializer<Money, CharSequence, MoneyLogicalTypeFactory>(
  logicalTypeClass = MoneyLogicalTypeFactory::class,
  conversion = MoneyConversion() // FIXME -> load from GenericData or load per SPI
)
