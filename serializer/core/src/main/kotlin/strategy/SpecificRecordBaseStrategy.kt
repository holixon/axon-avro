package io.holixon.axon.avro.serializer.strategy

import io.toolisticon.avro.kotlin.codec.SpecificRecordCodec
import org.apache.avro.generic.GenericData
import org.apache.avro.specific.SpecificRecordBase

class SpecificRecordBaseStrategy : AvroSerializationStrategy, AvroDeserializationStrategy {
  private val converter = SpecificRecordCodec.specificRecordToGenericRecordConverter()

  override fun canDeserialize(serializedType: Class<*>): Boolean = isGeneratedSpecificRecordBase(serializedType)

  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericData.Record): T {
    @Suppress("UNCHECKED_CAST")
    return SpecificRecordCodec.genericRecordToSpecificRecordConverter(serializedType).convert(data) as T
  }

  override fun canSerialize(serializedType: Class<*>): Boolean = isGeneratedSpecificRecordBase(serializedType)

  override fun serialize(data: Any): GenericData.Record = converter.convert(data as SpecificRecordBase)

  private fun isGeneratedSpecificRecordBase(serializedType: Class<*>): Boolean =
    SpecificRecordBase::class.java.isAssignableFrom(serializedType)
}
