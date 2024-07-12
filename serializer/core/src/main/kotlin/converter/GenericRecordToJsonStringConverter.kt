package io.holixon.axon.avro.serializer.converter

import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.codec.GenericRecordCodec
import io.toolisticon.kotlin.avro.value.JsonString
import org.apache.avro.generic.GenericRecord
import org.axonframework.serialization.ContentTypeConverter

class GenericRecordToJsonStringConverter : ContentTypeConverter<GenericRecord, JsonString> {
  override fun expectedSourceType(): Class<GenericRecord> = GenericRecord::class.java

  override fun targetType(): Class<JsonString> = JsonString::class.java

  override fun convert(original: GenericRecord): JsonString {
    return GenericRecordCodec.encodeJson(record = original, genericData = AvroKotlin.genericData)
  }

}
