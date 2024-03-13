package io.holixon.axon.avro.serializer.converter

import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.axonframework.serialization.ContentTypeConverter

class SingleObjectEncodedToByteArrayConverter : ContentTypeConverter<SingleObjectEncodedBytes, ByteArray> {
  override fun expectedSourceType(): Class<SingleObjectEncodedBytes> = SingleObjectEncodedBytes::class.java

  override fun targetType(): Class<ByteArray>  = ByteArray::class.java

  override fun convert(original: SingleObjectEncodedBytes): ByteArray = original.value
}
