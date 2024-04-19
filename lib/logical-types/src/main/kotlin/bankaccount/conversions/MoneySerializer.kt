package bankaccount.conversions

import io.toolisticon.avro.kotlin.logicaltypes.GeneralizedSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import org.javamoney.moneta.Money

/**
 * KSerializer for Money type from moneta library. Registered in the [MoneySerializerModule]
 */
@OptIn(ExperimentalSerializationApi::class)
class MoneySerializer : GeneralizedSerializer<Money, CharSequence, MoneyLogicalTypeFactory>(
  logicalTypeClass = MoneyLogicalTypeFactory::class
)
