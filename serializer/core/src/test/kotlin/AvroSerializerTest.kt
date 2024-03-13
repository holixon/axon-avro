package io.holixon.axon.avro.serializer

import bankaccount.event.BankAccountCreated
import com.github.avrokotlin.avro4k.Avro
import io.toolisticon.avro.kotlin.avroSchemaResolver
import io.toolisticon.avro.kotlin.codec.SpecificRecordCodec
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericData
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.serialization.SimpleSerializedObject
import org.axonframework.serialization.SimpleSerializedType
import org.junit.jupiter.api.Test


internal class AvroSerializerTest {
  private val schemaResolver = avroSchemaResolver(TestFixtures.BankAccountCreatedFixture.SCHEMA)

  @Test
  fun `canSerializeTo - genericRecord`() {

    val serializer = AvroSerializer.builder()
      .avroSchemaResolver(schemaResolver)
      .build()

    assertThat(serializer.canSerializeTo(GenericData.Record::class.java)).isTrue()
  }

  @Test
  fun `canSerializeTo - string`() {

    val serializer = AvroSerializer.builder()
      .avroSchemaResolver(schemaResolver)
      .build()

    assertThat(serializer.canSerializeTo(String::class.java)).isTrue()
  }


  @Test
  fun `canSerializeTo - singleObjectEncoded`() {

    val serializer = AvroSerializer.builder()
      .avroSchemaResolver(schemaResolver)
      .build()

    assertThat(serializer.canSerializeTo(SingleObjectEncodedBytes::class.java)).isTrue()
  }


  @Test
  fun `serialize specific record`() {
    val schemaResolver = avroSchemaResolver(AvroSchema(BankAccountCreated.getClassSchema()))
    val serializer = AvroSerializer.builder()
      .avroSchemaResolver(schemaResolver)
      .build()

    val data = BankAccountCreated.newBuilder()
      .setAccountId("1")
      .setInitialBalance(10)
      .build()

    val serialized = serializer.serialize(data, SingleObjectEncodedBytes::class.java)

    val bytes = serialized.data

    assertThat(SpecificRecordCodec.specificRecordSingleObjectDecoder(schemaResolver).decode(serialized.data)).isEqualTo(data)
  }

  @Test
  fun `deserialize singleObjectEncoded to specificRecord`() {
    val schemaResolver = avroSchemaResolver(AvroSchema(BankAccountCreated.getClassSchema()))
    val serializer = AvroSerializer.builder()
      .avroSchemaResolver(schemaResolver)
      .build()

    val data = BankAccountCreated.newBuilder()
      .setAccountId("1")
      .setInitialBalance(10)
      .build()

    val serializedObject = SimpleSerializedObject(
      TestFixtures.BankAccountCreatedFixture.SINGLE_OBJECT_ENCODED,
      SingleObjectEncodedBytes::class.java,
      SimpleSerializedType(BankAccountCreated::class.java.canonicalName, null)
    )

    val deserialized = serializer.deserialize<SingleObjectEncodedBytes, Any>(serializedObject)

    assertThat(deserialized).isInstanceOf(BankAccountCreated::class.java)
    with(deserialized as BankAccountCreated) {
      assertThat(accountId).isEqualTo("1")
      assertThat(initialBalance).isEqualTo(10)
    }
  }

  @Test
  fun `serialize and deserialize kotlinx data class`() {
    val bar = BarString("hello world")
    val avro4k = Avro.default

    val schemaResolver = avroSchemaResolver(AvroSchema(avro4k.schema(BarString.serializer())))
    val serializer = AvroSerializer.builder()
      .avroSchemaResolver(schemaResolver)
      .avro4k(avro4k)
      .build()

    val serialized = serializer.serialize(bar, ByteArray::class.java)

    val deserialized = serializer.deserialize<ByteArray, Any>(serialized)

    assertThat(deserialized).isInstanceOf(BarString::class.java)
    assertThat(deserialized).hasToString(bar.toString())
  }
}
