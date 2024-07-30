package io.holixon.axon.avro.serializer.strategy

import _ktx.ResourceKtx
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.serialization.strategy.GenericRecordSerializationStrategy
import io.toolisticon.kotlin.avro.value.Name.Companion.toName
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.axonframework.messaging.responsetypes.MultipleInstancesResponseType
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class MultipleInstancesResponseTypeStrategy : GenericRecordSerializationStrategy {
  companion object {
    val SCHEMA = AvroSchema.of(resource = ResourceKtx.resourceUrl("schema/AvroMultipleInstancesResponseType.avsc"))
    const val FIELD = "expectedResponseType"
    val FIELD_SCHEMA = SCHEMA.getField(FIELD.toName())!!.schema
  }


  override fun test(serializedType: KClass<*>): Boolean = MultipleInstancesResponseType::class == serializedType

  override fun <T : Any> deserialize(serializedType: KClass<*>, data: GenericRecord): T {
    // TODO we shouldn't bother the avro type utf8
    val className = data.get(FIELD) as Utf8

    return MultipleInstancesResponseType(Class.forName(className.toString())) as T
  }

  override fun <T : Any> serialize(data: T): GenericRecord {
    require(data is MultipleInstancesResponseType<*>)
    return AvroKotlin.createGenericRecord(SCHEMA) {
      put(FIELD, data.expectedResponseType.canonicalName)
    }
  }
}

//@Suppress("UNCHECKED_CAST")
//override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericData.Record): T {
//  return ConfigToken(data.get(ConfigTokenStrategy.FIELD_CONFIG) as Map<String,String>) as T
//}
//
//override fun canSerialize(serializedType: Class<*>): Boolean = ConfigToken::class.java == serializedType
//
//override fun serialize(data: Any): GenericData.Record {
//  require(data is ConfigToken)
//  require(isSchemaCompliant(data.config)) { "Data: $data not compliant with schema=${ConfigTokenStrategy.SCHEMA}" }
//  return AvroKotlin.createGenericRecord(ConfigTokenStrategy.SCHEMA) {
//    put(ConfigTokenStrategy.FIELD_CONFIG, data.config)
//  }
//}
//
//internal fun isSchemaCompliant(data: Any): Boolean = genericData.validate(ConfigTokenStrategy.SCHEMA_VALUES.get(), data)
//}
