package bankaccount.conversions

import org.apache.avro.LogicalType
import org.apache.avro.Schema
import org.javamoney.moneta.Money
import java.nio.ByteBuffer


class MoneyConversion : AbstractConversion<Money, CharSequence>(
  logicalTypeClass = MoneyLogicalTypeFactory::class,
  targetClass = Money::class
) {
  // correct methods need to be invoked
  override fun toCharSequence(value: Money, schema: Schema, type: LogicalType): CharSequence {
    return toAvro(value)
  }
  override fun fromCharSequence(value: CharSequence, schema: Schema, type: LogicalType): Money {
    return fromAvro(value)
  }

  override fun fromAvro(value: CharSequence): Money {
    val (amount, currencyCode) = value.split(" ".toPattern(), 2)
    return Money.of(amount.toBigDecimal(), currencyCode)
  }

  override fun toAvro(value: Money): CharSequence {
    return "${value.numberStripped} ${value.currency.currencyCode}"
  }
}
