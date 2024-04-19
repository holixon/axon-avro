package bankaccount.conversions

import kotlinx.serialization.ExperimentalSerializationApi
import org.javamoney.moneta.Money

@OptIn(ExperimentalSerializationApi::class)
class MoneySerializer : GeneralizedSerializer<Money, CharSequence, MoneyLogicalTypeFactory>(
  logicalTypeClass = MoneyLogicalTypeFactory::class,
  targetClass = Money::class
)
