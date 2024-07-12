package io.holixon.axon.avro.serializer.strategy

import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import kotlinx.serialization.Serializable
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord

class KotlinxDataClassStrategy(
  private val avroKotlinSerialization: AvroKotlinSerialization,
  private val genericData: GenericData
) : AvroSerializationStrategy, AvroDeserializationStrategy {

  override fun canDeserialize(serializedType: Class<*>): Boolean = isKotlinxDataClass(serializedType)

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericRecord): T {
    return avroKotlinSerialization.fromRecord(data, serializedType.kotlin) as T
  }

  override fun canSerialize(serializedType: Class<*>): Boolean = isKotlinxDataClass(serializedType)

  override fun serialize(data: Any): GenericRecord {
    return avroKotlinSerialization.toRecord(data)
  }

  private fun isKotlinxDataClass(serializedType: Class<*>): Boolean {
    // TODO: can this check be replaced by some convenience magic from kotlinx.serialization
    return serializedType.kotlin.isData
      && serializedType.annotations.any { it is Serializable }
  }
}
