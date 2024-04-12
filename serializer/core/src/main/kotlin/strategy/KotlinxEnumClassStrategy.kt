package io.holixon.axon.avro.serializer.strategy

import com.github.avrokotlin.avro4k.Avro
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchemaChecks.compatibleToReadFrom
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.apache.avro.generic.GenericData
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

class KotlinxEnumClassStrategy(
  private val avro4k: Avro,
  private val genericData: GenericData
) : AvroSerializationStrategy, AvroDeserializationStrategy {

  override fun canSerialize(serializedType: Class<*>): Boolean = isKotlinxEnumClass(serializedType)
  override fun canDeserialize(serializedType: Class<*>): Boolean = isKotlinxEnumClass(serializedType)

  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericData.Record): T {
    val writerSchema = AvroSchema(data.schema)

    val fn = serializedType.kotlin.companionObject?.functions?.find { it.name == "serializer" }!!
    @Suppress("UNCHECKED_CAST")
    val kserializer = fn.call(serializedType.kotlin.companionObjectInstance) as KSerializer<Any>
    val readerSchema = AvroSchema(avro4k.schema(kserializer))

    // TODO nicer?
    require( readerSchema.compatibleToReadFrom(writerSchema).result.incompatibilities.isEmpty()) {"Reader/writer schema incompatibleQ!"}

    @Suppress("UNCHECKED_CAST")
    return avro4k.fromRecord(kserializer, data) as T
  }


  override fun serialize(data: Any): GenericData.Record {
    val fn = data::class.companionObject?.functions?.find { it.name == "serializer" }!!
    @Suppress("UNCHECKED_CAST")
    val kserializer = fn.call(data::class.companionObjectInstance) as KSerializer<Any>

    return avro4k.toRecord(kserializer, data).let {
      // TODO: we could return interface GenericRecord and not do a deep copy/conversion here
      genericData.deepCopy(it.schema, it) as GenericData.Record
    }
  }

  private fun isKotlinxEnumClass(serializedType: Class<*>) : Boolean {
    // TODO: can this check be replaced by some convenience magic from kotlinx.serialization
    return serializedType.isEnum
      && serializedType.annotations.any { it is Serializable }
  }
}
