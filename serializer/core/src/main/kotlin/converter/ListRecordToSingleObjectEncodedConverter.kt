package io.holixon.axon.avro.serializer.converter

import com.github.avrokotlin.avro4k.ListRecord
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.codec.GenericRecordCodec
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import kotlinx.serialization.ExperimentalSerializationApi
import mu.KLogging
import org.axonframework.serialization.ContentTypeConverter

@OptIn(ExperimentalSerializationApi::class)
class ListRecordToSingleObjectEncodedConverter : ContentTypeConverter<ListRecord, SingleObjectEncodedBytes> {

  companion object : KLogging()

  override fun expectedSourceType(): Class<ListRecord> = ListRecord::class.java

  override fun targetType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun convert(original: ListRecord): SingleObjectEncodedBytes {
    // TODO: we (sh|c)ould make genericData/conversions configurable ... maybe.
    logger.trace { "Generic record: $original" }
    return GenericRecordCodec.encodeSingleObject(record = original, genericData = AvroKotlin.genericData)
  }
}
