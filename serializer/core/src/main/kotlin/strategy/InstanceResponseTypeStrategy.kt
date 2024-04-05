package io.holixon.axon.avro.serializer.strategy

import _ktx.ResourceKtx
import io.toolisticon.avro.kotlin.AvroKotlin
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.value.Name
import org.apache.avro.generic.GenericData
import org.apache.avro.util.Utf8
import org.axonframework.messaging.responsetypes.InstanceResponseType
import org.axonframework.messaging.responsetypes.MultipleInstancesResponseType

@Suppress("UNCHECKED_CAST")
class InstanceResponseTypeStrategy(
  val genericData: GenericData
) : AvroDeserializationStrategy, AvroSerializationStrategy {
  companion object {
    val SCHEMA = AvroSchema(resource = ResourceKtx.resourceUrl("schema/AvroInstanceResponseType.avsc"))
    const val FIELD = "expectedResponseType"
    val FIELD_SCHEMA = SCHEMA.getField(Name(FIELD))!!.schema
  }


  override fun canDeserialize(serializedType: Class<*>): Boolean = InstanceResponseType::class.java == serializedType

  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericData.Record): T {
    val className = data.get(FIELD) as Utf8

    return InstanceResponseType(Class.forName(className.toString())) as T
  }

  override fun canSerialize(serializedType: Class<*>): Boolean = InstanceResponseType::class.java == serializedType

  override fun serialize(data: Any): GenericData.Record {
    require(data is InstanceResponseType<*>)
    return AvroKotlin.createGenericRecord(SCHEMA) {
      put(FIELD, data.expectedResponseType.canonicalName)
    }
  }
}
