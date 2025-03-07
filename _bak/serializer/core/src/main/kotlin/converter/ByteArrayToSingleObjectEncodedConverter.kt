package io.holixon.axon.avro.serializer.converter

import io.toolisticon.kotlin.avro.value.ByteArrayValue
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import org.axonframework.serialization.ContentTypeConverter

class ByteArrayToSingleObjectEncodedConverter : ContentTypeConverter<ByteArray, SingleObjectEncodedBytes> {
  override fun expectedSourceType(): Class<ByteArray> = ByteArray::class.java

  override fun targetType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun convert(original: ByteArray): SingleObjectEncodedBytes = SingleObjectEncodedBytes.of(bytes = ByteArrayValue(original))
}
