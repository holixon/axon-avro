package io.holixon.axon.avro.serializer.converter

import io.toolisticon.avro.kotlin.codec.GenericRecordCodec
import org.apache.avro.generic.GenericData
import org.axonframework.serialization.ContentTypeConverter

class GenericRecordToJsonStringConverter : ContentTypeConverter<GenericData.Record, String> {
  override fun expectedSourceType(): Class<GenericData.Record> = GenericData.Record::class.java

  override fun targetType(): Class<String> = String::class.java

  override fun convert(original: GenericData.Record): String {
    return GenericRecordCodec.encodeJson(original).value
  }
}
