package bankaccount.conversions

import io.toolisticon.avro.kotlin.value.LogicalTypeName
import org.apache.avro.Schema
import org.javamoney.moneta.Money

class MoneyLogicalTypeFactory : AbstractAvroLogicalTypeBase<Money, CharSequence>(
  schemaType = Schema.Type.STRING,
  name = LogicalTypeName(NAME)
) {

  companion object {
    const val NAME = "money"
  }

  // needs to be implemented
  override fun toJvm(value: CharSequence): Money {
    val (amount, currencyCode) = value.split(" ".toPattern(), 2)
    return Money.of(amount.toBigDecimal(), currencyCode)
  }

  override fun toAvro(value: Money): CharSequence {
    return "${value.numberStripped} ${value.currency.currencyCode}"
  }
}

