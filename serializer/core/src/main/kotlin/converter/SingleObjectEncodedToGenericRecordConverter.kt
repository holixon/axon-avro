package io.holixon.axon.avro.serializer.converter

import io.toolisticon.kotlin.avro.codec.GenericRecordCodec
import io.toolisticon.kotlin.avro.repository.AvroSchemaResolver
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericRecord
import org.axonframework.serialization.ContentTypeConverter

class SingleObjectEncodedToGenericRecordConverter(
  private val schemaResolver: AvroSchemaResolver
) : ContentTypeConverter<SingleObjectEncodedBytes,GenericRecord> {
  override fun expectedSourceType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun targetType(): Class<GenericRecord> = GenericRecord::class.java

  override fun convert(singleObjectEncodedBytes: SingleObjectEncodedBytes): GenericRecord {
    val readerSchema = schemaResolver[singleObjectEncodedBytes.fingerprint]
    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    return GenericRecordCodec
      .decodeSingleObject(singleObjectEncodedBytes = singleObjectEncodedBytes, readerSchema = readerSchema)
  }
}
