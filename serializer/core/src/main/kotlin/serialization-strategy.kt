package io.holixon.axon.avro.serializer

import com.github.avrokotlin.avro4k.Avro
import io.toolisticon.avro.kotlin.codec.SpecificRecordCodec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.apache.avro.generic.GenericData
import org.apache.avro.specific.SpecificRecordBase
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions


interface AvroSerializationStrategy {

  fun canSerialize(serializedType: Class<*>): Boolean

  fun serialize(data: Any): GenericData.Record
}

class KotlinxDataClassSerializationStrategy(
  val avro4k: Avro,
  val genericData: GenericData
) : AvroSerializationStrategy {
  override fun canSerialize(serializedType: Class<*>): Boolean {
    // TODO: can this check be replaced by some convenience magic from kotlinx.serialization
    return serializedType.kotlin.isData
      && serializedType.annotations.any { it is Serializable }
  }

  override fun serialize(data: Any): GenericData.Record {
    val fn = data::class.companionObject?.functions?.find { it.name == "serializer" }!!
    val kserializer = fn.call(data::class.companionObjectInstance) as KSerializer<Any>

    return avro4k.toRecord(kserializer, data).let {
      // TODO: we could return interface GenericRecord and not do a deep copy/conversion here
      genericData.deepCopy(it.schema, it) as GenericData.Record
    }
  }
}

class SpecificRecordBaseSerializationStrategy : AvroSerializationStrategy {
  private val converter = SpecificRecordCodec.specificRecordToGenericRecordConverter()

  override fun canSerialize(serializedType: Class<*>): Boolean = SpecificRecordBase::class.java.isAssignableFrom(serializedType)

  override fun serialize(data: Any): GenericData.Record {
    return converter.convert(data as SpecificRecordBase)
  }

}
