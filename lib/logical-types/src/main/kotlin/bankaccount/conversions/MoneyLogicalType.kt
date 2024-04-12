package bankaccount.conversions

import io.toolisticon.avro.kotlin.logical.AvroLogicalType
import io.toolisticon.avro.kotlin.value.LogicalTypeName
import org.apache.avro.Conversion
import org.apache.avro.LogicalType
import org.apache.avro.LogicalTypes
import org.apache.avro.Schema
import org.javamoney.moneta.Money

class MoneyLogicalType : AvroLogicalType<Money> {
  companion object {
    const val NAME = "money"

    /**
     * Denotes the logical type known by Avro.
     */
    val INSTANCE: MoneyLogicalType by lazy {
      requireNotNull(LogicalTypes.getCustomRegisteredTypes()[NAME]) { "Cold not find custom logical type $NAME. Did you register it?"} as MoneyLogicalType
    }

    fun Money.toCharSequence() = "${this.numberStripped} ${this.currency.currencyCode}"
    fun CharSequence.toMoney(): Money {
      val (amount, currencyCode) = this.split(" ".toPattern(), 2)
      return Money.of(amount.toBigDecimal(), currencyCode)
    }
  }

  override val name: LogicalTypeName = LogicalTypeName(NAME)

  override val logicalType: LogicalType = object : LogicalType(NAME) {

    override fun validate(schema: Schema) {
      super.validate(schema)
      require(schema.type == Schema.Type.STRING) { "Only STRING is supported for logicalType=$NAME." }
    }
  }

  override val conversion: Conversion<Money> = MoneyConversion()

  override fun fromSchema(schema: Schema): LogicalType? = if (schema.getProp(LogicalType.LOGICAL_TYPE_PROP) == NAME) {
    logicalType
  } else {
    null
  }
}

