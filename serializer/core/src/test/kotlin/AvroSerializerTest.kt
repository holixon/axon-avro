package io.holixon.axon.avro.serializer

import bankaccount.event.BankAccountCreated
import io.holixon.axon.avro.serializer._test.BarString
import io.toolisticon.kotlin.avro.AvroKotlin.avroSchemaResolver
import io.toolisticon.kotlin.avro.codec.SpecificRecordCodec
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericRecord
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.serialization.SimpleSerializedObject
import org.axonframework.serialization.SimpleSerializedType
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test

internal class AvroSerializerTest {

  private val avroKotlinSerialization = AvroKotlinSerialization()
    .registerSchema(TestFixtures.BankAccountCreatedFixture.SCHEMA)

  @Test
  fun `canSerializeTo - genericRecord`() {

    val serializer = AvroSerializer.builder()
      .avroKotlinSerialization(avroKotlinSerialization)
      .build()

    assertThat(serializer.canSerializeTo(GenericRecord::class.java)).isTrue()
  }

  @Test
  fun `canSerializeTo - string`() {
    val serializer = AvroSerializer.builder()
      .avroKotlinSerialization(avroKotlinSerialization)
      .build()

    assertThat(serializer.canSerializeTo(String::class.java)).isTrue()
  }


  @Test
  fun `canSerializeTo - singleObjectEncoded`() {
    val serializer = AvroSerializer.builder()
      .avroKotlinSerialization(avroKotlinSerialization)
      .build()

    assertThat(serializer.canSerializeTo(SingleObjectEncodedBytes::class.java)).isTrue()
  }


  @Test
  fun `serialize specific record`() {
    val serializer = AvroSerializer.builder()
      .avroKotlinSerialization(avroKotlinSerialization.registerSchema(AvroSchema(BankAccountCreated.getClassSchema())))
      .build()

    val data = BankAccountCreated.newBuilder()
      .setAccountId("1")
      .setInitialBalance(Money.of(10, "EUR"))
      .build()

    val serialized = serializer.serialize(data, SingleObjectEncodedBytes::class.java)

    val bytes = serialized.data

    assertThat(SpecificRecordCodec.specificRecordSingleObjectDecoder(avroKotlinSerialization).decode(bytes)).isEqualTo(data)
  }

  @Test
  fun `deserialize singleObjectEncoded to specificRecord`() {
    val serializer = AvroSerializer.builder()
      .avroKotlinSerialization(
        avroKotlinSerialization
          .registerSchema(AvroSchema(BankAccountCreated.getClassSchema()))
      )
      .build()

//    val data = BankAccountCreated.newBuilder()
//      .setAccountId("1")
//      .setInitialBalance(Money.of(10, "EUR"))
//      .build()

    val serializedObject = SimpleSerializedObject(
      TestFixtures.BankAccountCreatedFixture.SINGLE_OBJECT_ENCODED,
      SingleObjectEncodedBytes::class.java,
      SimpleSerializedType(BankAccountCreated::class.java.canonicalName, null)
    )

    val deserialized = serializer.deserialize<SingleObjectEncodedBytes, Any>(serializedObject)

    assertThat(deserialized).isInstanceOf(BankAccountCreated::class.java)
    with(deserialized as BankAccountCreated) {
      assertThat(accountId).isEqualTo("1")
      assertThat(initialBalance).isEqualTo(Money.of(1, "EUR"))
    }
  }

  @Test
  fun `serialize and deserialize kotlinx data class`() {
    val bar = BarString("hello world")
    val avro = AvroKotlinSerialization()

    val schema = avro.schema(bar::class)
    println(schema)

    val schemaResolver = avroSchemaResolver(avro.schema(BarString::class))

    val serializer = AvroSerializer.builder()
      .avroKotlinSerialization(avro)
      .build()

    val serialized = serializer.serialize(bar, ByteArray::class.java)

    val deserialized = serializer.deserialize<ByteArray, Any>(serialized)

    assertThat(deserialized).isInstanceOf(BarString::class.java)
    assertThat(deserialized).hasToString(bar.toString())
  }
}
