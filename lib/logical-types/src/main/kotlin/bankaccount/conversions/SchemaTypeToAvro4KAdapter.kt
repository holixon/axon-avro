package bankaccount.conversions

import com.github.avrokotlin.avro4k.decoder.ExtendedDecoder
import com.github.avrokotlin.avro4k.encoder.ExtendedEncoder
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import org.apache.avro.Conversion
import org.apache.avro.LogicalType
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.util.Utf8
import java.nio.ByteBuffer
import kotlin.reflect.KClass

/**
 * Adapter between AVRO4K and Avro (Java) APIs.
 * Is used in the Avro4KSerializer to ease the delegation to Avro Conversion.
 */

/**
 * Primitive kind used for descriptor generation in serializer.
 */
fun Schema.Type.primitiveType(): PrimitiveKind {
  return when (this) {
    Schema.Type.STRING -> PrimitiveKind.STRING
    Schema.Type.FLOAT -> PrimitiveKind.FLOAT
    Schema.Type.LONG -> PrimitiveKind.LONG
    Schema.Type.INT -> PrimitiveKind.INT
    Schema.Type.BOOLEAN -> PrimitiveKind.BOOLEAN
    Schema.Type.DOUBLE -> PrimitiveKind.DOUBLE
    Schema.Type.BYTES -> PrimitiveKind.BYTE // FIXME: should we support it?
    else -> throw SerializationException("Unsupported schema type received: '$this'")
  }
}

/**
 * Schema required to be added by serializer as the base for the logical type.
 */
fun Schema.Type.schema(): Schema {
  return SchemaBuilder
    .builder()
    .let {
      when (this) {
        Schema.Type.STRING -> it.stringType()
        Schema.Type.FLOAT -> it.floatType()
        Schema.Type.LONG -> it.longType()
        Schema.Type.INT -> it.intType()
        Schema.Type.BOOLEAN -> it.booleanType()
        Schema.Type.DOUBLE -> it.doubleType()
        Schema.Type.BYTES -> it.bytesType() // FIXME: should we support it?
        else -> throw SerializationException("Unsupported schema type received: '$this'")
      }
    }
}

fun ExtendedEncoder.encodeTypedValue(schemaType: Schema.Type, value: Any) {
  when (schemaType) {
    Schema.Type.STRING -> this.encodeString(value as String)
    Schema.Type.FLOAT -> this.encodeFloat(value as Float)
    Schema.Type.LONG -> this.encodeLong(value as Long)
    Schema.Type.INT -> this.encodeInt(value as Int)
    Schema.Type.BOOLEAN -> this.encodeBoolean(value as Boolean)
    Schema.Type.DOUBLE -> this.encodeDouble(value as Double)
    Schema.Type.BYTES -> this.encodeByteArray(ByteBuffer.wrap(value as ByteArray)) // FIXME asymmetric ?! -> understand this... maybe we should not support it at all.
    else -> throw SerializationException("Unsupported schema type '$this'")
  }
}

fun ExtendedDecoder.decode(targetClass: KClass<out Any>): Any {
  return when (targetClass.java) {
    Utf8::class.java -> this.decodeString()
    Float::class.java -> this.decodeFloat()
    Long::class.java -> this.decodeLong()
    Int::class.java -> this.decodeInt()
    Boolean::class.java -> this.decodeBoolean()
    Double::class.java -> this.decodeDouble()
    Short::class.java -> this.decodeShort()
    ByteBuffer::class.java -> this.decodeByte() // FIXME asymmetric ?! -> understand this... maybe we should not support it at all.
    else -> throw SerializationException("Could not decode the value of type $targetClass")
  }
}


/**
 * Delivers methods of [Conversion<T>] to invoke in order to encode value to Avro representation.
 */
fun <T : Any> Schema.Type.conversionMethodToAvro(): (Conversion<T>, T, Schema, LogicalType) -> Any {
  return when (this) {
    Schema.Type.STRING -> Conversion<T>::toCharSequence
    Schema.Type.FLOAT -> Conversion<T>::toFloat
    Schema.Type.LONG -> Conversion<T>::toLong
    Schema.Type.INT -> Conversion<T>::toInt
    Schema.Type.BOOLEAN -> Conversion<T>::toBoolean
    Schema.Type.DOUBLE -> Conversion<T>::toDouble
    Schema.Type.BYTES -> Conversion<T>::toBytes
    else -> throw IllegalArgumentException("Unsupported target AVRO type '${this}'.")
  }
}

/**
 * Invokes the selected method to encode value to Avro representation.
 */
fun <T: Any, AVRO_TYPE: Any> Conversion<T>.invokeConversionMethodToAvro(value: T, schema: Schema, logicalTypeInstance: AbstractAvroLogicalTypeBase<T, AVRO_TYPE>): AVRO_TYPE {
  val method = logicalTypeInstance.schemaType.conversionMethodToAvro<T>()
  @Suppress("UNCHECKED_CAST")
  return method.invoke(this, value, schema, logicalTypeInstance.logicalType) as AVRO_TYPE
}

/**
 * Invokes correct method to decode from Avro representation.
 * Note, we can't deliver those methods because the return type can't be created in Kotlin. So we create the invocation directly.
 */
fun <T : Any, AVRO_TYPE : Any> Conversion<T>.invokeConversionMethodFromAvro(value: AVRO_TYPE, schema: Schema, logicalType: LogicalType): T {
  return when (value) {
    is CharSequence -> Conversion<T>::fromCharSequence.invoke(this, value, schema, logicalType)
    is Float -> Conversion<T>::fromFloat.invoke(this, value, schema, logicalType)
    is Long -> Conversion<T>::fromLong.invoke(this, value, schema, logicalType)
    is Int -> Conversion<T>::fromInt.invoke(this, value, schema, logicalType)
    is Boolean -> Conversion<T>::fromBoolean.invoke(this, value, schema, logicalType)
    is Double -> Conversion<T>::fromDouble.invoke(this, value, schema, logicalType)
    is ByteBuffer -> Conversion<T>::fromBytes.invoke(this, value, schema, logicalType)
    else -> throw IllegalArgumentException("Unsupported value '$value' of AVRO type '${value::class.qualifiedName}'.")
  }
}
