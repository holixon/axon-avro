package io.holixon.axon.avro.serializer.converter

import com.github.avrokotlin.avro4k.ListRecord
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.codec.GenericRecordCodec
import io.toolisticon.kotlin.avro.value.JsonString
import kotlinx.serialization.ExperimentalSerializationApi
import org.axonframework.serialization.ContentTypeConverter

@OptIn(ExperimentalSerializationApi::class)
class ListRecordToJsonStringConverter : ContentTypeConverter<ListRecord, JsonString> {
  override fun expectedSourceType(): Class<ListRecord> = ListRecord::class.java

  override fun targetType(): Class<JsonString> = JsonString::class.java

  override fun convert(original: ListRecord): JsonString {
    return GenericRecordCodec.encodeJson(record = original, genericData = AvroKotlin.genericData)
  }

}
