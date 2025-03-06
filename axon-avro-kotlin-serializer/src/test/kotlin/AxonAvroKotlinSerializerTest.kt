package io.holixon.axon.avro.serializer

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.avro.serializer.AxonAvroKotlinSerializerHelper.REVISION_RESOLVER_BLANK
import io.holixon.axon.avro.serializer.AxonAvroKotlinSerializerHelper.REVISION_RESOLVER_NULL
import io.holixon.axon.avro.serializer.AxonAvroKotlinSerializerHelper.SchemaJson.compatibleSchema
import io.holixon.axon.avro.serializer.AxonAvroKotlinSerializerHelper.SchemaJson.compatibleSchemaWithAdditionalField
import io.holixon.axon.avro.serializer.AxonAvroKotlinSerializerHelper.SchemaJson.compatibleSchemaWithoutValue2
import io.holixon.axon.avro.serializer.AxonAvroKotlinSerializerHelper.SchemaJson.incompatibleSchema
import io.holixon.axon.avro.serializer._test.ComplexObject
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import org.apache.avro.generic.GenericRecord
import org.apache.avro.message.BinaryMessageEncoder
import org.apache.avro.message.SchemaStore
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.axonframework.common.AxonConfigurationException
import org.axonframework.messaging.MetaData
import org.axonframework.serialization.*
import org.axonframework.serialization.avro.AvroSerializer
import org.axonframework.serialization.avro.AvroUtil
import org.axonframework.serialization.json.JacksonSerializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.io.ByteArrayOutputStream
import java.io.InputStream

private val logger = KotlinLogging.logger {}

class AxonAvroKotlinSerializerTest {
  private lateinit var serializer: AvroSerializer
  private lateinit var serializerDelegate: Serializer
  private lateinit var avro: AvroKotlinSerialization

  @BeforeEach
  fun setUp() {
    avro = AvroKotlinSerialization()

    serializerDelegate = spy<JacksonSerializer>(JacksonSerializer.defaultSerializer())

    serializer = AvroSerializer.builder()
      .serializerDelegate(serializerDelegate)
      .revisionResolver(REVISION_RESOLVER_NULL)
      .schemaStore(avro)
      .includeSchemasInStackTraces(true)
      .performSchemaCompatibilityCheck(true)
      .addSerializerStrategy(AvroKotlinSerializerStrategy(avro = avro, revisionResolver = REVISION_RESOLVER_NULL))
      .build()
  }

  @Test
  fun testBuilderMandatoryValues() {
//    assertThatThrownBy {
//      AvroSerializer.builder()
//        .includeDefaultAvroSerializationStrategies(false)
//        .build()
//    }.isInstanceOf(AxonConfigurationException::class.java)
//      .hasMessage("RevisionResolver is mandatory")

    assertThatThrownBy {
      AvroSerializer.builder()
        .revisionResolver(REVISION_RESOLVER_BLANK)
        .build()
    }.isInstanceOf(AxonConfigurationException::class.java)
      .hasMessage("SchemaStore is mandatory")

    assertThatThrownBy {
      AvroSerializer.builder()
        .revisionResolver(REVISION_RESOLVER_BLANK)
        .schemaStore(SchemaStore.Cache())
        .build()
    }.isInstanceOf(AxonConfigurationException::class.java)
      .hasMessage("SerializerDelegate is mandatory")
  }

  @Test
  fun deliverUnknownClassIfTypeIsNotOnClasspath() {
    val clazz =
      serializer.classForType(SimpleSerializedType("org.acme.Foo", null))

    assertThat(clazz).isEqualTo(UnknownSerializedType::class.java)
  }

  @Test
  fun deliverEmptyType() {
    assertThat(serializer.typeForClass(null))
      .isEqualTo(SimpleSerializedType.emptyType())

    assertThat(serializer.typeForClass(Void::class.java))
      .isEqualTo(SimpleSerializedType.emptyType())
  }

  @Test
  fun deliverNonEmptyType() {
    assertThat(serializer.typeForClass(String::class.java))
      .isEqualTo(
        SimpleSerializedType(
          String::class.java.getCanonicalName(),
          REVISION_RESOLVER_NULL.revisionOf(String::class.java)
        )
      )
  }

  @Test
  fun canSerialize() {
    assertThat(serializer.canSerializeTo<ByteArray>(ByteArray::class.java)).isTrue
    assertThat(serializer.canSerializeTo<GenericRecord>(GenericRecord::class.java)).isTrue

    assertThat(serializer.canSerializeTo<String?>(String::class.java)).isTrue
    assertThat(serializer.canSerializeTo<InputStream?>(InputStream::class.java)).isTrue
    assertThat(serializer.canSerializeTo<Int?>(Int::class.java)).isFalse
  }

  @Test
  fun serializeMetaDataByDelegate() {
    val original = MetaData.from(mapOf<String, String>("test" to "test"))

    val serialized = serializer.serialize<ByteArray>(original, ByteArray::class.java)
    val actual = serializer.deserialize<ByteArray, MetaData?>(serialized)

    assertThat(actual).isNotNull
    assertThat(actual).hasSize(1)
    assertThat(actual["test"]).isEqualTo("test")

    verify<Serializer?>(serializerDelegate).serialize<ByteArray>(original, ByteArray::class.java)
    verify<Serializer?>(serializerDelegate).deserialize<ByteArray, kotlin.Any?>(serialized)
  }

  @Test
  fun serializeNullByDelegate() {
    serializer.serialize<ByteArray>(null, ByteArray::class.java)

    verify(serializerDelegate).serialize<ByteArray>(null, ByteArray::class.java)
  }

  @Test
  fun deserializeEmptyBytes() {
    assertThat(serializer.classForType(SerializedType.emptyType())).isEqualTo(Void::class.java)
  }

  @Test
  fun serializeAndDeserializeComplexObject() {
    avro.registerSchema(avro.schema(ComplexObject::class))

    val serialized =
      serializer.serialize<ByteArray>(complexObject, ByteArray::class.java)

    assertThat(serialized.type.name).isEqualTo(ComplexObject::class.java.getCanonicalName())

    val deserialized: ComplexObject? = serializer.deserialize<ByteArray, ComplexObject?>(serialized)
    assertThat(deserialized).isEqualTo(complexObject)
  }

  @Test
  fun serializeFromCompatibleObjectAndDeserialize() {
    avro.registerSchema(avro.schema(ComplexObject::class))
    val schema = AvroKotlin.parseSchema(compatibleSchema)
    avro.registerSchema(schema)
    val record: GenericRecord = AvroKotlin.createGenericRecord(schema) {
      put("value1", complexObject.value1)
      put("value2", complexObject.value2)
      put("value3", complexObject.value3)
    }

    val encodedBytes: ByteArray = genericRecordToByteArray(record)

    val serialized: SerializedObject<ByteArray> = createSerializedObject(
      encodedBytes,
      ComplexObject::class.java.getCanonicalName()
    )

    assertThat(serialized.type.name)
      .isEqualTo(ComplexObject::class.java.getCanonicalName())

    val deserialized: ComplexObject? = serializer.deserialize<ByteArray, ComplexObject?>(serialized)
    assertThat(deserialized).isEqualTo(complexObject)
  }

  @Test
  fun deserializeFromGenericRecord() {
    avro.registerSchema(avro.schema(ComplexObject::class))

    val schema = AvroKotlin.parseSchema(compatibleSchema)
    avro.registerSchema(schema)
    val record: GenericRecord = AvroKotlin.createGenericRecord(schema) {
      put("value1", complexObject.value1)
      put("value2", complexObject.value2)
      put("value3", complexObject.value3)
    }

    val serialized: SerializedObject<GenericRecord> = createSerializedObject(record)

    val deserialized = serializer.deserialize<GenericRecord, ComplexObject>(serialized)
    assertThat(deserialized).isEqualTo(complexObject)
  }

  @Test
  fun serializeFromCompatibleWithAdditionalIgnoredFieldObjectAndDeserialize() {
    avro.registerSchema(avro.schema(ComplexObject::class))

    avro.registerSchema(avro.schema(ComplexObject::class))
    val schema = AvroKotlin.parseSchema(compatibleSchemaWithAdditionalField)
    avro.registerSchema(schema)
    val record: GenericRecord = AvroKotlin.createGenericRecord(schema) {
      put("value1", complexObject.value1)
      put("value2", complexObject.value2)
      put("value3", complexObject.value3)
      put("value4", "ignored value")
    }

    val encodedBytes: ByteArray = genericRecordToByteArray(record)

    val serialized: SerializedObject<ByteArray> = createSerializedObject(
      encodedBytes,
      ComplexObject::class.java.getCanonicalName()
    )

    assertThat(serialized.type.name).isEqualTo(ComplexObject::class.java.getCanonicalName())

    val deserialized: ComplexObject? = serializer.deserialize<ByteArray, ComplexObject?>(serialized)
    assertThat(deserialized).isEqualTo(complexObject)
  }

  @Test
  fun serializeFromCompatibleSchemaAndDeserializeUsingDefault() {
    avro.registerSchema(avro.schema(ComplexObject::class))
    val schema = AvroKotlin.parseSchema(compatibleSchemaWithoutValue2)
    avro.registerSchema(schema)
    val record: GenericRecord = AvroKotlin.createGenericRecord(schema) {
      put("value1", complexObject.value1)
      put("value3", complexObject.value3)
    }

    val encodedBytes: ByteArray = genericRecordToByteArray(record)

    val serialized: SerializedObject<ByteArray> = createSerializedObject(
      encodedBytes,
      ComplexObject::class.java.getCanonicalName()
    )
    assertThat(serialized.getType().getName()).isEqualTo(ComplexObject::class.java.getCanonicalName())

    val deserialized: ComplexObject = serializer.deserialize<ByteArray, ComplexObject>(serialized)
    assertThat(deserialized.value2).isEqualTo("default value")
  }

  @Test
  fun failsWhenSerializeFromIncompatibleSchemaAndDeserialize() {
    avro.registerSchema(avro.schema(ComplexObject::class))

    val writerSchema = AvroKotlin.parseSchema(incompatibleSchema)
    avro.registerSchema(writerSchema)
    val record: GenericRecord = AvroKotlin.createGenericRecord(writerSchema) {
      put("value2", complexObject.value1)
      put("value3", complexObject.value3)
    }

    val encodedBytes: ByteArray = genericRecordToByteArray(record)

    val serialized: SerializedObject<ByteArray> = createSerializedObject(
      encodedBytes,
      ComplexObject::class.java.getCanonicalName()
    )

    assertThat(serialized.getType().name).isEqualTo(ComplexObject::class.java.getCanonicalName())

    assertThatThrownBy { serializer.deserialize<ByteArray, Any>(serialized) }
      .isInstanceOf(SerializationException::class.java)
      .hasMessageStartingWith(
        "Failed to deserialize single-object-encoded bytes to instance of io.holixon.axon.avro.serializer._test.ComplexObject,"
      )
  }

  @Test
  fun failToDeserializeIfClassIsNotAvailable() {
    val writerSchema = AvroKotlin.parseSchema(incompatibleSchema)
    avro.registerSchema(writerSchema)

    val record: GenericRecord = AvroKotlin.createGenericRecord(writerSchema) {
      put("value2", complexObject.value1)
      put("value3", complexObject.value3)
    }

    val serializedObject: SimpleSerializedObject<GenericRecord> = SimpleSerializedObject<GenericRecord>(
      record,
      GenericRecord::class.java,
      SimpleSerializedType("org.acme.Foo", null)
    )
    val deserialized = serializer.deserialize<GenericRecord, Any>(serializedObject)

    assertThat(deserialized.javaClass).isEqualTo(UnknownSerializedType::class.java)
  }


  companion object {
    private val complexObject: ComplexObject = ComplexObject("foo", "bar", 42)

    private fun genericRecordToByteArray(genericRecord: GenericRecord): ByteArray {
      try {
        ByteArrayOutputStream().use { baos ->
          val encoder: BinaryMessageEncoder<GenericRecord> = BinaryMessageEncoder<GenericRecord>(
            AvroUtil.genericData,
            genericRecord.getSchema()
          )
          encoder.encode(genericRecord, baos)
          return baos.toByteArray()
        }
      } catch (e: java.lang.Exception) {
        throw java.lang.RuntimeException(e)
      }
    }

    private fun createSerializedObject(payload: ByteArray, objectType: String?): SerializedObject<ByteArray> {
      return SimpleSerializedObject<ByteArray>(
        payload,
        ByteArray::class.java,
        SimpleSerializedType(objectType, null)
      )
    }

    private fun createSerializedObject(record: GenericRecord): SerializedObject<GenericRecord> {
      return SimpleSerializedObject<GenericRecord>(
        record,
        GenericRecord::class.java,
        SimpleSerializedType(record.schema.fullName, REVISION_RESOLVER_NULL.revisionOf(GenericRecord::class.java))
      )
    }
  }
}
