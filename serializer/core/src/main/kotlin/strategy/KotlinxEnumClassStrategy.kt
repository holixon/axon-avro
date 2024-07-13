package io.holixon.axon.avro.serializer.strategy

import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import kotlinx.serialization.Serializable
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord

class KotlinxEnumClassStrategy(
  private val avroKotlinSerialization: AvroKotlinSerialization
) : AvroSerializationStrategy, AvroDeserializationStrategy {

  override fun canSerialize(serializedType: Class<*>): Boolean = isKotlinxEnumClass(serializedType)
  override fun canDeserialize(serializedType: Class<*>): Boolean = isKotlinxEnumClass(serializedType)

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericRecord): T {
    return avroKotlinSerialization.fromRecord(record = data, type = serializedType.kotlin) as T
  }

  override fun serialize(data: Any): GenericRecord {
    return avroKotlinSerialization.toRecord(data = data)
  }

  private fun isKotlinxEnumClass(serializedType: Class<*>) : Boolean {
    // TODO: can this check be replaced by some convenience magic from kotlinx.serialization
    return serializedType.isEnum
      && serializedType.annotations.any { it is Serializable }
  }
}
