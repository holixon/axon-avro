package io.holixon.axon.avro.serializer.converter

import io.toolisticon.avro.kotlin.AvroKotlin.defaultLogicalTypeConversions
import io.toolisticon.avro.kotlin.codec.GenericRecordCodec
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import mu.KLogging
import org.apache.avro.generic.GenericData
import org.axonframework.serialization.ContentTypeConverter

class GenericRecordToSingleObjectEncodedConverter : ContentTypeConverter<GenericData.Record, SingleObjectEncodedBytes> {

  companion object: KLogging()

  override fun expectedSourceType(): Class<GenericData.Record> = GenericData.Record::class.java

  override fun targetType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun convert(original: GenericData.Record): SingleObjectEncodedBytes {
    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    logger.info {" Generic: $original" }
    return GenericRecordCodec.encodeSingleObject(
      record = original,
      genericData = defaultLogicalTypeConversions.genericData
    )
  }
}
