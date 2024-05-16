package io.holixon.axon.avro.serializer.strategy

import _ktx.ResourceKtx
import io.toolisticon.avro.kotlin.AvroKotlin
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.value.Name
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.axonframework.messaging.responsetypes.MultipleInstancesResponseType

@Suppress("UNCHECKED_CAST")
class MultipleInstancesResponseTypeStrategy(
  val genericData: GenericData
) : AvroDeserializationStrategy, AvroSerializationStrategy {
  companion object {
    val SCHEMA = AvroSchema(resource = ResourceKtx.resourceUrl("schema/AvroMultipleInstancesResponseType.avsc"))
    const val FIELD = "expectedResponseType"
    val FIELD_SCHEMA = SCHEMA.getField(Name(FIELD))!!.schema
  }


  override fun canDeserialize(serializedType: Class<*>): Boolean = MultipleInstancesResponseType::class.java == serializedType

  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericRecord): T {
    val className = data.get(FIELD) as Utf8

    return MultipleInstancesResponseType(Class.forName(className.toString())) as T
  }

  override fun canSerialize(serializedType: Class<*>): Boolean = MultipleInstancesResponseType::class.java == serializedType

  override fun serialize(data: Any): GenericRecord {
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
