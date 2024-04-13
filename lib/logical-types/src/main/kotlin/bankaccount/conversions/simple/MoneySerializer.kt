package bankaccount.conversions.simple

import com.github.avrokotlin.avro4k.decoder.ExtendedDecoder
import com.github.avrokotlin.avro4k.encoder.ExtendedEncoder
import com.github.avrokotlin.avro4k.schema.AvroDescriptor
import com.github.avrokotlin.avro4k.schema.NamingStrategy
import com.github.avrokotlin.avro4k.serializer.AvroSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
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
    when (schema.type) {
      Schema.Type.STRING -> encoder.encodeString(MoneyLogicalType.INSTANCE.toAvro(obj) as String)
      else -> throw SerializationException("Only ${Schema.Type.STRING} is supported.")
    }
  }

  override fun decodeAvroValue(schema: Schema, decoder: ExtendedDecoder): Money {
    return when (val any = decoder.decodeAny()) {
      is String -> MoneyLogicalType.INSTANCE.toJvm(decoder.decodeString())
      is Money -> any
      else -> throw SerializationException("Could not decode $any")
    }
  }

  override val descriptor: SerialDescriptor = object : AvroDescriptor(MoneyLogicalType.NAME, PrimitiveKind.STRING) {
    override fun schema(annos: List<Annotation>, serializersModule: SerializersModule, namingStrategy: NamingStrategy): Schema {
      val schema = SchemaBuilder.builder().stringType()
      return MoneyLogicalType().logicalType.addToSchema(schema) // TODO -> we can't use the INSTANCE, because this looks up in internal Avro Logical Types collection and it is not registered there
    }
  }
}


