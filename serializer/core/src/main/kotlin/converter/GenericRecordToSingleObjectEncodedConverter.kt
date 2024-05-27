package io.holixon.axon.avro.serializer.converter

import io.toolisticon.kotlin.avro.codec.GenericRecordCodec
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericRecord
import org.axonframework.serialization.ContentTypeConverter

class GenericRecordToSingleObjectEncodedConverter : ContentTypeConverter<GenericRecord, SingleObjectEncodedBytes> {

  override fun expectedSourceType(): Class<GenericRecord> = GenericRecord::class.java

  override fun targetType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun convert(original: GenericRecord): SingleObjectEncodedBytes {
    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    return GenericRecordCodec.encodeSingleObject(original)
  }
}
