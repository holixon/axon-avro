package io.holixon.axon.avro.serializer.strategy

import _ktx.ResourceKtx
import io.toolisticon.avro.kotlin.AvroKotlin
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.value.Name
import org.apache.avro.generic.GenericData
import org.axonframework.eventhandling.tokenstore.ConfigToken

class ConfigTokenStrategy(
  private val genericData : GenericData
) : AvroSerializationStrategy, AvroDeserializationStrategy {
  companion object {
    val SCHEMA = AvroSchema(resource = ResourceKtx.resourceUrl("schema/AvroConfigToken.avsc"))
    const val FIELD_CONFIG = "config"
    val SCHEMA_VALUES = SCHEMA.getField(Name(FIELD_CONFIG))!!.schema
  }

  override fun canDeserialize(serializedType: Class<*>): Boolean = ConfigToken::class.java == serializedType

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericData.Record): T {
    return ConfigToken(data.get(FIELD_CONFIG) as Map<String,String>) as T
  }

  override fun canSerialize(serializedType: Class<*>): Boolean = ConfigToken::class.java == serializedType

  override fun serialize(data: Any): GenericData.Record {
    require(data is ConfigToken)
    require(isSchemaCompliant(data.config)) { "Data: $data not compliant with schema=$SCHEMA" }
    return AvroKotlin.createGenericRecord(SCHEMA) {
      put(FIELD_CONFIG, data.config)
    }
  }

  internal fun isSchemaCompliant(data: Any): Boolean = genericData.validate(SCHEMA_VALUES.get(), data)
}
