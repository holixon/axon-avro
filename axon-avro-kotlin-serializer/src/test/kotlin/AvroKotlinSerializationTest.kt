package io.holixon.axon.avro.serializer

import io.holixon.axon.avro.serializer._test.BarString
import io.holixon.axon.avro.serializer._test.barStringSchema
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import io.toolisticon.kotlin.avro.serialization.isKotlinxDataClass
import io.toolisticon.kotlin.avro.serialization.isSerializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AvroKotlinSerializationTest {

  private val avro = AvroKotlinSerialization()

  @Test
  fun `get schema from BarString`() {
    assertThat(avro.cachedSchemaClasses()).isEmpty()
    assertThat(avro.cachedSerializerClasses()).isEmpty()

    val schema = avro.schema(BarString::class)

    assertThat(schema.fingerprint).isEqualTo(barStringSchema.fingerprint)

    assertThat(avro.cachedSchemaClasses()).containsExactly(BarString::class)
    assertThat(avro.cachedSerializerClasses()).containsExactly(BarString::class)

    assertThat(avro[barStringSchema.fingerprint]).isEqualTo(schema)

    val data = BarString("foo")
    val encoded = avro.encodeToSingleObjectEncoded(data)

    val decoded = avro.decodeFromSingleObjectEncoded<BarString>(encoded)

    assertThat(decoded).isEqualTo(data)
  }


  @Test
  fun `barString is kotlinx serializable`() {
    assertThat(BarString::class.isSerializable()).isTrue()
    assertThat(BarString::class.isKotlinxDataClass()).isTrue()
    assertThat(avro.serializer(BarString::class)).isNotNull
    assertThat(avro.schema(BarString::class)).isNotNull
  }
}
