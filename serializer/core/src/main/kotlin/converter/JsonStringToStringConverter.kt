package io.holixon.axon.avro.serializer.converter

import io.toolisticon.avro.kotlin.value.JsonString
import org.axonframework.serialization.ContentTypeConverter


class JsonStringToStringConverter : ContentTypeConverter<JsonString, String> {
  override fun expectedSourceType(): Class<JsonString> = JsonString::class.java

  override fun targetType(): Class<String> = String::class.java

  override fun convert(original: JsonString): String = original.value
}
