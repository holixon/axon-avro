package io.holixon.axon.avro.serializer.converter

import io.toolisticon.avro.kotlin.AvroSchemaResolver
import io.toolisticon.avro.kotlin.codec.GenericRecordCodec
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericData
import org.axonframework.serialization.ContentTypeConverter

class SingleObjectEncodedToGenericRecordConverter(
  private val schemaResolver: AvroSchemaResolver
) : ContentTypeConverter<SingleObjectEncodedBytes, GenericData.Record> {
  override fun expectedSourceType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun targetType(): Class<GenericData.Record> = GenericData.Record::class.java

  override fun convert(singleObjectEncodedBytes: SingleObjectEncodedBytes): GenericData.Record {
    val readerSchema = schemaResolver[singleObjectEncodedBytes.fingerprint]

    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    return GenericRecordCodec.decodeSingleObject(singleObjectEncodedBytes = singleObjectEncodedBytes, readerSchema = readerSchema)
  }
}
