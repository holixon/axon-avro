package io.holixon.axon.avro.serializer.plugin

import io.toolisticon.avro.kotlin.AvroSchemaResolver
import io.toolisticon.avro.kotlin.codec.AvroCodec
import io.toolisticon.avro.kotlin.codec.GenericRecordCodec
import io.toolisticon.avro.kotlin.value.JsonString
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.apache.avro.message.MissingSchemaException

// FIXME -> replace with GenericRecordCodec.convert()
class SingleObjectToJsonConverter(
  private val avroSchemaResolver: AvroSchemaResolver
) : AvroCodec.Converter<SingleObjectEncodedBytes, JsonString> {

  @Throws(MissingSchemaException::class)
  override fun convert(source: SingleObjectEncodedBytes): JsonString {
    val record = GenericRecordCodec.decodeSingleObject(
      singleObjectEncodedBytes = source,
      readerSchema = avroSchemaResolver[source.fingerprint],
    )
    return GenericRecordCodec.encodeJson(record)
  }
}
