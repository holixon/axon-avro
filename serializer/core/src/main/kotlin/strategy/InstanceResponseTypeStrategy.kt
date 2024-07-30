package io.holixon.axon.avro.serializer.strategy

import _ktx.ResourceKtx
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.serialization.strategy.GenericRecordSerializationStrategy
import io.toolisticon.kotlin.avro.value.Name.Companion.toName
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.axonframework.messaging.responsetypes.InstanceResponseType
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class InstanceResponseTypeStrategy() : GenericRecordSerializationStrategy {
  companion object {
    val SCHEMA = AvroSchema.of(resource = ResourceKtx.resourceUrl("schema/AvroInstanceResponseType.avsc"))
    const val FIELD = "expectedResponseType"
    val FIELD_SCHEMA = SCHEMA.getField(FIELD.toName())!!.schema
  }

  override fun test(serializedType: KClass<*>): Boolean = InstanceResponseType::class.java == serializedType

  override fun <T : Any> deserialize(serializedType: KClass<*>, data: GenericRecord): T {
    val className = data.get(FIELD) as Utf8
    return InstanceResponseType(Class.forName(className.toString())) as T
  }

  override fun <T:Any> serialize(data: T): GenericRecord {
    require(data is InstanceResponseType<*>)
    return AvroKotlin.createGenericRecord(SCHEMA) {
      put(FIELD, data.expectedResponseType.canonicalName)
    }
  }
}
