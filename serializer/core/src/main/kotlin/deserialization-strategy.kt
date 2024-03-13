package io.holixon.axon.avro.serializer

import com.github.avrokotlin.avro4k.Avro
import io.toolisticon.avro.kotlin.codec.SpecificRecordCodec
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.apache.avro.generic.GenericData
import org.apache.avro.specific.SpecificRecordBase
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

interface AvroDeserializationStrategy {

  fun canDeserialize(serializedType: Class<*>) : Boolean

  fun <T: Any> deserialize(serializedType: Class<*>, data: GenericData.Record) : T

}

class KotlinxDataClassDeserializationStrategy(
  val avro4k: Avro
) : AvroDeserializationStrategy {

  override fun canDeserialize(serializedType: Class<*>): Boolean {
    // TODO: can this check be replaced by some convenience magic from kotlinx.serialization
    return serializedType.kotlin.isData
      && serializedType.annotations.any { it is Serializable }
  }

  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericData.Record): T {
    val writerSchema = AvroSchema(data.schema)

    val fn = serializedType.kotlin.companionObject?.functions?.find { it.name == "serializer" }!!
    val kserializer = fn.call(serializedType.kotlin.companionObjectInstance) as KSerializer<Any>
    val readerSchema = AvroSchema(avro4k.schema(kserializer))

    // TODO nicer?
    require( readerSchema.compatibleToReadFrom(writerSchema).result.incompatibilities.isEmpty()) {"Reader/writer schema incompatibleQ!"}

    return avro4k.fromRecord(kserializer, data) as T
  }

}

class SpecificRecordBaseDeserializationStrategy : AvroDeserializationStrategy {
  override fun canDeserialize(serializedType: Class<*>): Boolean = SpecificRecordBase::class.java.isAssignableFrom(serializedType)

  override fun <T : Any> deserialize(serializedType: Class<*>, data: GenericData.Record): T {
    return SpecificRecordCodec.genericRecordToSpecificRecordConverter(serializedType).convert(data) as T
  }

}
