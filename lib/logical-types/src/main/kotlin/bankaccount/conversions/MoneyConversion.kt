package bankaccount.conversions

import bankaccount.conversions.MoneyLogicalType.Companion.NAME
import bankaccount.conversions.MoneyLogicalType.Companion.toCharSequence
import bankaccount.conversions.MoneyLogicalType.Companion.toMoney
import org.apache.avro.Conversion
import org.apache.avro.LogicalType
import org.apache.avro.Schema
import org.javamoney.moneta.Money

class MoneyConversion : Conversion<Money>() {
  override fun getConvertedType(): Class<Money> = Money::class.java

  override fun getLogicalTypeName(): String = NAME

  override fun getRecommendedSchema(): Schema = Schema.create(Schema.Type.STRING).apply {
    MoneyLogicalType.INSTANCE.logicalType.addToSchema(this)
  }

  override fun fromCharSequence(value: CharSequence, schema: Schema, type: LogicalType): Money {
    return value.toMoney()
  }

  override fun toCharSequence(value: Money, schema: Schema, type: LogicalType): CharSequence {
    return value.toCharSequence()
  }
}
