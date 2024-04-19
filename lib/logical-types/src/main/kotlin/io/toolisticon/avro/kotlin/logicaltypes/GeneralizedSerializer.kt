package io.toolisticon.avro.kotlin.logicaltypes

import com.github.avrokotlin.avro4k.decoder.ExtendedDecoder
import com.github.avrokotlin.avro4k.encoder.ExtendedEncoder
import com.github.avrokotlin.avro4k.schema.AvroDescriptor
import com.github.avrokotlin.avro4k.schema.NamingStrategy
import com.github.avrokotlin.avro4k.serializer.AvroSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.SerializersModule
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * [T] JVM type: money
 * [AVRO_TYPE] Avro type (CharSequence)
 * [LOGICAL_TYPE] Logical Type
 */
@ExperimentalSerializationApi
abstract class GeneralizedSerializer<T : Any, AVRO_TYPE : Any, LOGICAL_TYPE : AbstractAvroLogicalTypeBase<T, AVRO_TYPE>>(
  val logicalTypeClass: KClass<LOGICAL_TYPE>
) : AvroSerializer<T>() {

  val logicalTypeInstance = logicalTypeClass.createInstance()

  /*
   * Conversion is resolved using the service loader.
   */
  private val conversion: AbstractConversion<T, AVRO_TYPE> =
    GenericData.get().getConversionFor<T>(logicalTypeInstance.logicalType).let { conversion ->
      require(conversion is AbstractConversion<*, *>) { "Found conversion of wrong type. It must be a subtype of ${AbstractConversion::class}" }
      @Suppress("UNCHECKED_CAST")
      return@let conversion as AbstractConversion<T, AVRO_TYPE>
    }

  override fun encodeAvroValue(schema: Schema, encoder: ExtendedEncoder, obj: T) {
    encoder.encodeTypedValue(schema.type, conversion.toAvro(obj, schema, logicalTypeInstance.logicalType))
  }

  override fun decodeAvroValue(schema: Schema, decoder: ExtendedDecoder): T {
    val any = requireNotNull(decoder.decodeAny()) { "Decode any must not return null" }
    return if (any::class.java == conversion.convertedType) {
      @Suppress("UNCHECKED_CAST")
      any as T
    } else {
      @Suppress("UNCHECKED_CAST")
      conversion.fromAvro(decoder.decode(any::class) as AVRO_TYPE, schema, logicalTypeInstance.logicalType)
    }
  }

  override val descriptor: SerialDescriptor = object : AvroDescriptor(
    type = logicalTypeClass,
    kind = logicalTypeInstance.schemaType.primitiveType()
  ) {
    override fun schema(annos: List<Annotation>, serializersModule: SerializersModule, namingStrategy: NamingStrategy): Schema {
      val schema: Schema = logicalTypeInstance.schemaType.schema()
      return logicalTypeInstance.logicalType.addToSchema(schema)
    }
  }

}
