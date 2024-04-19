package bankaccount.conversions

import com.github.avrokotlin.avro4k.decoder.ExtendedDecoder
import com.github.avrokotlin.avro4k.encoder.ExtendedEncoder
import com.github.avrokotlin.avro4k.schema.AvroDescriptor
import com.github.avrokotlin.avro4k.schema.NamingStrategy
import com.github.avrokotlin.avro4k.serializer.AvroSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.util.Utf8
import kotlin.reflect.KClass

/**
 * [T] JVM type: money
 * [AVRO4K_TYPE] Avro4k raw type (CharSequence)
 * [LOGICAL_TYPE] Logical Type
 */
@ExperimentalSerializationApi
abstract class GeneralizedSerializer<T : Any, AVRO4K_TYPE : Any, LOGICAL_TYPE : AbstractAvroLogicalTypeBase<T, AVRO4K_TYPE>>(
  val logicalTypeClass: KClass<LOGICAL_TYPE>,
  private val targetClass: KClass<T>,
) : AvroSerializer<T>() {

  val logicalTypeInstance = logicalTypeClass.constructors.first().call() // call default constructor

  // FIXME -> move to custom mapping
  val primitiveKind =
    when (logicalTypeInstance.schemaType) {
      Schema.Type.STRING -> PrimitiveKind.STRING
      Schema.Type.FLOAT -> PrimitiveKind.FLOAT
      Schema.Type.LONG -> PrimitiveKind.LONG
      Schema.Type.INT -> PrimitiveKind.INT
      Schema.Type.BOOLEAN -> PrimitiveKind.BOOLEAN
      else -> throw UnsupportedOperationException("Unknown schema type ${logicalTypeInstance.schemaType}")
    }

  override fun encodeAvroValue(schema: Schema, encoder: ExtendedEncoder, obj: T) {
    // FIXME -> move to custom mapping
    when (schema.type) {
      Schema.Type.STRING -> encoder.encodeString(logicalTypeInstance.toAvro(obj) as String)
      Schema.Type.FLOAT -> encoder.encodeFloat(logicalTypeInstance.toAvro(obj) as Float)
      Schema.Type.LONG -> encoder.encodeLong(logicalTypeInstance.toAvro(obj) as Long)
      Schema.Type.INT -> encoder.encodeInt(logicalTypeInstance.toAvro(obj) as Int)
      Schema.Type.BOOLEAN -> encoder.encodeBoolean(logicalTypeInstance.toAvro(obj) as Boolean)
      else -> throw SerializationException("Unsupported schema type ${schema.type}.")
    }
  }

  override fun decodeAvroValue(schema: Schema, decoder: ExtendedDecoder): T {
    val any = requireNotNull(decoder.decodeAny()) { "Decode any must not return null" }
    @Suppress("UNCHECKED_CAST") // two times unchecked cast
    return when (any::class) {
      targetClass -> any as T
      Utf8::class -> logicalTypeInstance.toJvm(decoder.decodeString() as AVRO4K_TYPE)
      else -> throw SerializationException("Could not decode $any of type ${any::class}")
    }
  }

  override val descriptor: SerialDescriptor = object : AvroDescriptor(logicalTypeClass, primitiveKind) {
    override fun schema(annos: List<Annotation>, serializersModule: SerializersModule, namingStrategy: NamingStrategy): Schema {
      val schema: Schema = SchemaBuilder.builder().let {
        when (logicalTypeInstance.schemaType) {
          Schema.Type.STRING -> it.stringType()
          // FIXME -> map all types
          else -> throw UnsupportedOperationException("Unknown schema type ${logicalTypeInstance.schemaType}")
        }
      }
      return logicalTypeInstance.logicalType.addToSchema(schema)
    }
  }

}
