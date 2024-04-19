package bankaccount.conversions

import org.apache.avro.LogicalType
import org.apache.avro.Schema
import org.javamoney.moneta.Money

/**
 * Logical type conversion for Money type from moneta library.
 */
class MoneyConversion : AbstractConversion<Money, CharSequence>(
  logicalTypeClass = MoneyLogicalTypeFactory::class,
  targetClass = Money::class
) {

  override fun toCharSequence(value: Money, schema: Schema, type: LogicalType): CharSequence {
    return "${value.numberStripped} ${value.currency.currencyCode}"
  }

  override fun fromCharSequence(value: CharSequence, schema: Schema, type: LogicalType): Money {
    val (amount, currencyCode) = value.split(" ".toPattern(), 2)
    return Money.of(amount.toBigDecimal(), currencyCode)
  }
}
