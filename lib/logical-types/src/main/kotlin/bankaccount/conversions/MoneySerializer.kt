package bankaccount.conversions

import io.toolisticon.avro.kotlin.logicaltypes.GeneralizedSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import org.javamoney.moneta.Money

/**
 * KSerializer for Money type from moneta library. Registered in the [MoneySerializerModule]
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Money::class)
class MoneySerializer : GeneralizedSerializer<Money, CharSequence, MoneyLogicalTypeFactory>(
  logicalTypeClass = MoneyLogicalTypeFactory::class
)
