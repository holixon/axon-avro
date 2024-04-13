package bankaccount.conversions

import io.toolisticon.avro.kotlin.value.LogicalTypeName
import kotlinx.serialization.ExperimentalSerializationApi
import org.apache.avro.Conversion
import org.apache.avro.LogicalType
import org.apache.avro.LogicalTypes
import org.apache.avro.Schema
import org.javamoney.moneta.Money

class MoneyLogicalType : AbstractAvroLogicalTypeBase<Money, CharSequence>(
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

  class CoolAvroConversion : AbstractConversion<Money, CharSequence>(
    logicalTypeClass = MoneyLogicalType::class,
    targetClass = Money::class,
    logicalTypeName = NAME
  ) {
    // correct methods need to be invoked
    override fun toCharSequence(value: Money, schema: Schema, type: LogicalType): CharSequence = toAvro4K(value)
    override fun fromCharSequence(value: CharSequence, schema: Schema, type: LogicalType): Money = toJvm(value)
  }

  /**
   * This is working
   */
  class AvroConversion : Conversion<Money>() {

    companion object {
      private val registeredInstance by lazy {
        requireNotNull(LogicalTypes.getCustomRegisteredTypes()[NAME]) { "Cold not find custom logical type $NAME. Did you register it?" } as MoneyLogicalType
      }

      private val instance by lazy {
        MoneyLogicalType()
      }
    }

    override fun getConvertedType(): Class<Money> {
      return Money::class.java
    }

    override fun getLogicalTypeName(): String {
      return registeredInstance.typeName
    }

    override fun getRecommendedSchema(): Schema {
      return Schema
        .create(registeredInstance.schemaType)
        .apply {
          registeredInstance.logicalType.addToSchema(this)
        }
    }

    override fun fromCharSequence(value: CharSequence, schema: Schema, type: LogicalType): Money {
      return instance.toJvm(value)
    }

    override fun toCharSequence(value: Money, schema: Schema, type: LogicalType): CharSequence {
      return instance.toAvro(value)
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  class Serializer : GeneralizedSerializer<Money, CharSequence, MoneyLogicalType>(
    logicalTypeClass = MoneyLogicalType::class,
    targetClass = Money::class
  )
}

