package io.holixon.axon.avro.serializer.converter

import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.codec.GenericRecordCodec
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import mu.KLogging
import org.apache.avro.generic.GenericData
import org.axonframework.serialization.ContentTypeConverter

class GenericDataRecordToSingleObjectEncodedConverter : ContentTypeConverter<GenericData.Record, SingleObjectEncodedBytes> {

  companion object : KLogging()

  override fun expectedSourceType(): Class<GenericData.Record> = GenericData.Record::class.java

  override fun targetType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun convert(original: GenericData.Record): SingleObjectEncodedBytes {
    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    logger.trace { "Generic record: $original" }
    return GenericRecordCodec.encodeSingleObject(record = original, genericData = AvroKotlin.genericData)
  }
}
