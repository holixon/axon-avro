package io.holixon.axon.avro.serializer.converter

import io.toolisticon.avro.kotlin.codec.GenericRecordCodec
import io.toolisticon.avro.kotlin.value.JsonString
import org.apache.avro.generic.GenericData
import org.axonframework.serialization.ContentTypeConverter

class GenericRecordToJsonStringConverter : ContentTypeConverter<GenericData.Record, JsonString> {
  override fun expectedSourceType(): Class<GenericData.Record> = GenericData.Record::class.java

  override fun targetType(): Class<JsonString> = JsonString::class.java

  override fun convert(original: GenericData.Record): JsonString {
    return GenericRecordCodec.encodeJson(original)
  }

}
