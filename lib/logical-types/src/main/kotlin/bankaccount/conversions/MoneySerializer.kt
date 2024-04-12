package bankaccount.conversions

import bankaccount.conversions.MoneyLogicalType.Companion.toCharSequence
import bankaccount.conversions.MoneyLogicalType.Companion.toMoney
import com.github.avrokotlin.avro4k.decoder.ExtendedDecoder
import com.github.avrokotlin.avro4k.encoder.ExtendedEncoder
import com.github.avrokotlin.avro4k.schema.AvroDescriptor
import com.github.avrokotlin.avro4k.schema.NamingStrategy
import com.github.avrokotlin.avro4k.serializer.AvroSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.javamoney.moneta.Money

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Money::class)
class MoneySerializer : AvroSerializer<Money>() {

  override fun encodeAvroValue(schema: Schema, encoder: ExtendedEncoder, obj: Money) {
    encoder.encodeString(obj.toCharSequence())
  }

  override fun decodeAvroValue(schema: Schema, decoder: ExtendedDecoder): Money {
    return decoder.decodeString().toMoney()
  }

  override val descriptor: SerialDescriptor = object : AvroDescriptor(MoneyLogicalType.NAME, PrimitiveKind.STRING) {
    override fun schema(annos: List<Annotation>, serializersModule: SerializersModule, namingStrategy: NamingStrategy): Schema {
      val schema = SchemaBuilder.builder().stringType()
      return MoneyLogicalType().logicalType.addToSchema(schema) // TODO -> we can't use the INSTANCE, because this looks up in internal Avro Logical Types collection and it is not registered there
    }
  }
}
