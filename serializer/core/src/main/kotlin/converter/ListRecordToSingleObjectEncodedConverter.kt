package io.holixon.axon.avro.serializer.converter

import com.github.avrokotlin.avro4k.ListRecord
import io.toolisticon.avro.kotlin.AvroKotlin.defaultLogicalTypeConversions
import io.toolisticon.avro.kotlin.codec.GenericRecordCodec
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import mu.KLogging
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.axonframework.serialization.ContentTypeConverter

class ListRecordToSingleObjectEncodedConverter : ContentTypeConverter<ListRecord, SingleObjectEncodedBytes> {

  companion object: KLogging()

  override fun expectedSourceType(): Class<ListRecord> = ListRecord::class.java

  override fun targetType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun convert(original: ListRecord): SingleObjectEncodedBytes {
    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    logger.trace {"Generic record: $original" }
    return GenericRecordCodec.encodeSingleObject(
      record = original,
      genericData = defaultLogicalTypeConversions.genericData
    )
  }
}
