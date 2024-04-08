package io.holixon.axon.avro.serializer.converter

import io.toolisticon.avro.kotlin.codec.GenericRecordCodec
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericData
import org.axonframework.serialization.ContentTypeConverter

class GenericRecordToSingleObjectEncodedConverter : ContentTypeConverter<GenericData.Record, SingleObjectEncodedBytes> {

  override fun expectedSourceType(): Class<GenericData.Record> = GenericData.Record::class.java

  override fun targetType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun convert(original: GenericData.Record): SingleObjectEncodedBytes {
    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    return GenericRecordCodec.encodeSingleObject(original)
  }
}
