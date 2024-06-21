package io.holixon.axon.avro.serializer.strategy

import _ktx.ResourceKtx
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.value.Name.Companion.toName
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.axonframework.messaging.MetaData

class MetaDataStrategy(
  private val genericData: GenericData
) : AvroSerializationStrategy, AvroDeserializationStrategy {
  companion object {
    val SCHEMA = AvroSchema.of(resource = ResourceKtx.resourceUrl("schema/AvroMetaData.avsc"))
    const val FIELD_VALUES = "values"
    val SCHEMA_VALUES = SCHEMA.getField(FIELD_VALUES.toName())!!.schema
  }

  override fun canDeserialize(serializedType: Class<*>): Boolean = MetaData::class.java == serializedType

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericRecord): T {
    return MetaData.from(data.get(FIELD_VALUES) as Map<String, *>) as T
  }

  override fun canSerialize(serializedType: Class<*>): Boolean = MetaData::class.java == serializedType

  override fun serialize(data: Any): GenericRecord {
    require(isSchemaCompliant(data)) { "Data: $data not compliant with schema=$SCHEMA" }
    return AvroKotlin.createGenericRecord(SCHEMA) {
      put(FIELD_VALUES, data)
    }
  }

  internal fun isSchemaCompliant(data: Any): Boolean = genericData.validate(SCHEMA_VALUES.get(), data)
}