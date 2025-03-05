package io.holixon.axon.avro.serializer

import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import io.toolisticon.kotlin.avro.serialization.isKotlinxDataClass
import io.toolisticon.kotlin.avro.value.AvroFingerprint
import io.toolisticon.kotlin.avro.value.ByteArrayValue
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericRecord
import org.axonframework.serialization.*
import org.axonframework.serialization.avro.AvroSerializerStrategy
import org.axonframework.serialization.avro.AvroUtil

class AvroKotlinSerializerStrategy(
  private val avro: AvroKotlinSerialization,
  private val includeSchemasInStackTraces: Boolean = true,
  private val revisionResolver: RevisionResolver
) : AvroSerializerStrategy {

  override fun test(payloadType: Class<*>) = payloadType.kotlin.isKotlinxDataClass()

  @Throws(SerializationException::class)
  override fun serializeToSingleObjectEncoded(value: Any): SerializedObject<ByteArray> = try {
    SimpleSerializedObject(
      avro.encodeToSingleObjectEncoded(value).value,
      ByteArray::class.java,
      SimpleSerializedType(value::class.java.canonicalName, revisionResolver.revisionOf(value::class.java))
    )
  } catch (e: Exception) {
    throw SerializationException(e.message, e)
  }

  @Throws(SerializationException::class)
  override fun <T : Any> deserializeFromSingleObjectEncoded(
    serializedObject: SerializedObject<ByteArray>, type: Class<T>
  ): T = try {
    val soe = SingleObjectEncodedBytes.of(serializedObject.data)
    avro.decodeFromSingleObjectEncoded(soe, type.kotlin)
  } catch (e: Exception) {
    throw AvroUtil.createExceptionFailedToDeserialize(
      type,
      avro.schema(type.kotlin).get(),
      avro[AvroFingerprint.of(ByteArrayValue(serializedObject.data))].get(),
      e,
      includeSchemasInStackTraces
    )
  }

  override fun <T : Any> deserializeFromGenericRecord(
    serializedObject: SerializedObject<GenericRecord>,
    type: Class<T>
  ): T {
    return avro.decodeFromGenericRecord(serializedObject.data, type.kotlin)
  }
}
