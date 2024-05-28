package io.holixon.axon.avro.serializer.strategy

import com.github.avrokotlin.avro4k.Avro
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchemaChecks.compatibleToReadFrom
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

class KotlinxDataClassStrategy(
  private val avro4k: Avro,
  private val genericData: GenericData
) : AvroSerializationStrategy, AvroDeserializationStrategy {

  override fun canDeserialize(serializedType: Class<*>): Boolean = isKotlinxDataClass(serializedType)

  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericRecord): T {
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

  override fun canSerialize(serializedType: Class<*>): Boolean = isKotlinxDataClass(serializedType)

  override fun serialize(data: Any): GenericRecord {
    val fn = data::class.companionObject?.functions?.find { it.name == "serializer" }!!
    @Suppress("UNCHECKED_CAST")
    val kserializer = fn.call(data::class.companionObjectInstance) as KSerializer<Any>

    return avro4k.toRecord(kserializer, data)
  }

  private fun isKotlinxDataClass(serializedType: Class<*>) : Boolean {
    // TODO: can this check be replaced by some convenience magic from kotlinx.serialization
    return serializedType.kotlin.isData
      && serializedType.annotations.any { it is Serializable }
  }
}
