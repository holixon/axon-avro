package bankaccount.conversions

import io.toolisticon.kotlin.avro.logical.StringLogicalType
import io.toolisticon.kotlin.avro.logical.StringLogicalTypeFactory
import io.toolisticon.kotlin.avro.logical.conversion.StringLogicalTypeConversion
import io.toolisticon.kotlin.avro.logical.conversion.TypeConverter
import io.toolisticon.kotlin.avro.serialization.serializer.StringLogicalTypeSerializer
import io.toolisticon.kotlin.avro.serialization.spi.AvroSerializerModuleFactory
import io.toolisticon.kotlin.avro.value.LogicalTypeName.Companion.toLogicalTypeName
import kotlinx.serialization.modules.SerializersModule
import org.javamoney.moneta.Money
import java.util.*
import javax.money.format.AmountFormatQuery
import javax.money.format.MonetaryFormats


object MoneyLogicalType : StringLogicalType("money".toLogicalTypeName()) {

  val convertedType = Money::class
  val conversion = MoneyConversion()

  val converter = object : TypeConverter<String, Money> {
    private val format = MonetaryFormats.getAmountFormat(AmountFormatQuery.of(Locale.GERMAN))

    override fun fromAvro(value: String): Money {
      return Money.from(format.parse(value))
    }

    override fun toAvro(value: Money): String {
      return format.format(value)
    }
  }

  class MoneyLogicalTypeFactory : StringLogicalTypeFactory<MoneyLogicalType>(logicalType = MoneyLogicalType)

  class MoneyConversion : StringLogicalTypeConversion<MoneyLogicalType, Money>(
    logicalType = MoneyLogicalType,
    convertedType = convertedType
  ) {
    override fun fromAvro(value: String): Money = converter.fromAvro(value)
    override fun toAvro(value: Money): String = converter.toAvro(value)
  }

  class MoneySerializer : StringLogicalTypeSerializer<MoneyLogicalType, Money>(conversion)

  class MoneySerializerModuleFactory : AvroSerializerModuleFactory {
    override fun invoke(): SerializersModule = SerializersModule {
      contextual(convertedType, MoneySerializer())
    }
  }
}
