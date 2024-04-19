package bankaccount.conversions

import org.apache.avro.LogicalType
import org.apache.avro.Schema
import org.javamoney.moneta.Money

class MoneyConversion : AbstractConversion<Money, CharSequence>(
  logicalTypeClass = MoneyLogicalTypeFactory::class,
  targetClass = Money::class,
  logicalTypeName = MoneyLogicalTypeFactory.NAME
) {
  // correct methods need to be invoked
  override fun toCharSequence(value: Money, schema: Schema, type: LogicalType): CharSequence = toAvro4K(value)
  override fun fromCharSequence(value: CharSequence, schema: Schema, type: LogicalType): Money = toJvm(value)
}
