package io.holixon.axon.avro.serializer.strategy

import io.holixon.axon.avro.serializer.strategy.test.ComplexObject
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import io.toolisticon.kotlin.avro.value.JsonString
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.message.BinaryMessageEncoder
import org.apache.avro.message.SchemaStore
import org.axonframework.common.AxonConfigurationException
import org.axonframework.messaging.MetaData
import org.axonframework.serialization.*
import org.axonframework.serialization.avro.AvroSerializer
import org.axonframework.serialization.avro.AvroUtil
import org.axonframework.serialization.json.JacksonSerializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

class AvroSerializerTest {
    private val revisionResolver = RevisionResolver { payloadType -> null }
    private lateinit var testSubject: AvroSerializer
    private lateinit var serializer: Serializer
    private lateinit var avro: AvroKotlinSerialization

    @BeforeEach
    fun setUp() {
        avro = AvroKotlinSerialization()

        serializer = spy<JacksonSerializer>(JacksonSerializer.defaultSerializer())
        testSubject = AvroSerializer
            .builder()
            .serializerDelegate(serializer)
            .revisionResolver(revisionResolver)
            .schemaStore(avro)
            .addSerializerStrategy(AvroKotlinSerializerStrategy(avro, revisionResolver))
            .build()
    }

    @Test
    fun testBuilderMandatoryValues() {
        val revisionResolverMandatory =
            assertThrows<AxonConfigurationException>(
                AxonConfigurationException::class.java,
                Executable {
                    AvroSerializer.builder().build()
                })
        assertEquals(
            "RevisionResolver is mandatory",
            revisionResolverMandatory.message
        )

        val schemaStoreMandatory =
            assertThrows<AxonConfigurationException>(
                AxonConfigurationException::class.java,
                Executable {
                    AvroSerializer.builder()
                        .revisionResolver(RevisionResolver { c: java.lang.Class<*>? -> "" })
                        .build()
                })
        assertEquals("SchemaStore is mandatory", schemaStoreMandatory.message)

        val serializerDelegateMandatory =
            assertThrows<AxonConfigurationException>(
                AxonConfigurationException::class.java,
                Executable {
                    AvroSerializer.builder()
                        .revisionResolver(RevisionResolver { c: java.lang.Class<*>? -> "" })
                        .schemaStore(SchemaStore.Cache())
                        .build()
                })
        assertEquals(
            "SerializerDelegate is mandatory",
            serializerDelegateMandatory.message
        )
    }

    @Test
    fun deliverUnknownClassIfTypeIsNotOnClasspath() {
        val clazz =
            testSubject!!.classForType(SimpleSerializedType("org.acme.Foo", null))
        assertEquals(
            org.axonframework.serialization.UnknownSerializedType::class.java,
            clazz
        )
    }

    @Test
    fun deliverEmptyType() {
        assertEquals(
            SimpleSerializedType.emptyType(),
            testSubject!!.typeForClass(null)
        )
        assertEquals(
            SimpleSerializedType.emptyType(),
            testSubject!!.typeForClass(Void::class.java)
        )
    }

    @Test
    fun deliverNonEmptyType() {
        assertEquals(
            SimpleSerializedType(
                kotlin.String::class.java.getCanonicalName(),
                revisionResolver.revisionOf(kotlin.String::class.java)
            ),
            testSubject!!.typeForClass(kotlin.String::class.java)
        )
    }


    @Test
    fun canSerialize() {
        assertTrue(testSubject!!.canSerializeTo<ByteArray>(ByteArray::class.java))
        assertTrue(testSubject!!.canSerializeTo<GenericRecord>(GenericRecord::class.java))

        assertTrue(testSubject!!.canSerializeTo<kotlin.String?>(kotlin.String::class.java))
        assertTrue(testSubject!!.canSerializeTo<java.io.InputStream?>(java.io.InputStream::class.java))
        assertFalse(testSubject!!.canSerializeTo<kotlin.Int?>(kotlin.Int::class.java))
    }

    @Test
    fun serializeMetaDataByDelegate() {
        val original = MetaData.from(mapOf<String, String>("test" to "test"))

        val serialized = testSubject!!.serialize<ByteArray>(original, ByteArray::class.java)
        val actual = testSubject!!.deserialize<ByteArray, MetaData?>(serialized)

        assertNotNull(actual)
        assertEquals("test", actual!!.get("test"))
        assertEquals(1, actual.size)

        verify<Serializer?>(serializer).serialize<ByteArray>(original, ByteArray::class.java)
        verify<Serializer?>(serializer).deserialize<ByteArray, kotlin.Any?>(serialized)
    }

    @Test
    fun serializeNull() {
        val npe = assertThrows<NullPointerException>(
            NullPointerException::class.java,
            Executable {
                testSubject!!.serialize<ByteArray>(
                    null,
                    ByteArray::class.java
                )
            })
        assertEquals("Can't serialize a null object", npe.message)
    }

    @Test
    fun deserializeEmptyBytes() {
        assertEquals(
            Void::class.java,
            testSubject!!.classForType(SerializedType.emptyType())
        )
        org.junit.jupiter.api.Assertions.assertNull(
            testSubject!!.deserialize<ByteArray, kotlin.Any?>(
                org.axonframework.serialization.SimpleSerializedObject<ByteArray>(
                    ByteArray(0),
                    ByteArray::class.java,
                    SerializedType.emptyType()
                )
            )
        )
    }

    @Test
    fun serializeAndDeserializeComplexObject() {
        avro.registerSchema(avro.schema(ComplexObject::class))

        val serialized =
            testSubject!!.serialize<ByteArray>(complexObject, ByteArray::class.java)
        assertEquals(
            serialized.getType().getName(),
            ComplexObject::class.java.getCanonicalName()
        )


        val deserialized: ComplexObject? = testSubject!!.deserialize<ByteArray, ComplexObject?>(serialized)
        assertEquals(complexObject, deserialized)
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
        assertEquals(
            serialized.getType().getName(),
            ComplexObject::class.java.getCanonicalName()
        )

        val deserialized: ComplexObject? = testSubject!!.deserialize<ByteArray, ComplexObject?>(serialized)
        assertEquals(complexObject, deserialized)
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


        val serialized: org.axonframework.serialization.SerializedObject<GenericRecord> =
            AvroSerializerTest.Companion.createSerializedObject(record)

        val deserialized: ComplexObject? = testSubject.deserialize<GenericRecord, ComplexObject?>(serialized)
        assertEquals(complexObject, deserialized)
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

        val encodedBytes: ByteArray = AvroSerializerTest.Companion.genericRecordToByteArray(record)

        val serialized: org.axonframework.serialization.SerializedObject<ByteArray> =
            AvroSerializerTest.Companion.createSerializedObject(
                encodedBytes,
                ComplexObject::class.java.getCanonicalName()
            )
        assertEquals(
            serialized.getType().getName(),
            ComplexObject::class.java.getCanonicalName()
        )

        val deserialized: ComplexObject? = testSubject!!.deserialize<ByteArray, ComplexObject?>(serialized)
        assertEquals(complexObject, deserialized)
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
        assertEquals(
            serialized.getType().getName(),
            ComplexObject::class.java.getCanonicalName()
        )

        val deserialized: ComplexObject = testSubject.deserialize<ByteArray, ComplexObject>(serialized)
        assertEquals("default value", deserialized.value2)
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
        assertEquals(
            serialized.getType().getName(),
            ComplexObject::class.java.getCanonicalName()
        )


        val exception: SerializationException = assertThrows<SerializationException>(
            SerializationException::class.java,
            Executable { testSubject.deserialize<ByteArray, Any>(serialized) })
        assertEquals(
            exception.message,
            AvroUtil.createExceptionFailedToDeserialize(
                ComplexObject::class.java,
                ComplexObject.getClassSchema(),
                writerSchema.get(),
                null
            ).message
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


        val `object`: org.axonframework.serialization.SimpleSerializedObject<GenericRecord> =
            org.axonframework.serialization.SimpleSerializedObject<GenericRecord>(
                record,
                GenericRecord::class.java,
                SimpleSerializedType("org.acme.Foo", null)
            )
        val deserialized = testSubject.deserialize<GenericRecord, kotlin.Any>(`object`)
        assertEquals(
            UnknownSerializedType::class.java,
            deserialized.javaClass
        )
    }


    companion object {
        private val compatibleSchemaWithoutValue2: JsonString = JsonString.of(
            "{\n" +
                    "  \"name\": \"ComplexObject\",\n" +
                    "  \"namespace\": \"io.holixon.axon.avro.serializer.strategy.test\",\n" +
                    "  \"type\": \"record\",\n" +
                    "  \"fields\": [\n" +
                    "    {\n" +
                    "      \"name\": \"value1\",\n" +
                    "      \"type\": \"string\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"value3\",\n" +
                    "      \"type\": \"int\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
        )

        private val incompatibleSchema = JsonString.of(
            "{\n" +
                    "  \"name\": \"ComplexObject\",\n" +
                    "  \"namespace\": \"io.holixon.axon.avro.serializer.strategy.test\",\n" +
                    "  \"type\": \"record\",\n" +
                    "  \"fields\": [\n" +
                    "    {\n" +
                    "      \"name\": \"value2\",\n" +
                    "      \"type\": \"string\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"value3\",\n" +
                    "      \"type\": \"int\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
        )


        private val compatibleSchema = JsonString.of(
            "{\n" +
                    "  \"name\": \"ComplexObject\",\n" +
                    "  \"namespace\": \"io.holixon.axon.avro.serializer.strategy.test\",\n" +
                    "  \"type\": \"record\",\n" +
                    "  \"fields\": [\n" +
                    "    {\n" +
                    "      \"name\": \"value1\",\n" +
                    "      \"type\": \"string\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"value2\",\n" +
                    "      \"type\": \"string\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"value3\",\n" +
                    "      \"type\": \"int\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
        )

        private val compatibleSchemaWithAdditionalField = JsonString.of(
            "{\n" +
                    "  \"name\": \"ComplexObject\",\n" +
                    "  \"namespace\": \"io.holixon.axon.avro.serializer.strategy.test\",\n" +
                    "  \"type\": \"record\",\n" +
                    "  \"fields\": [\n" +
                    "    {\n" +
                    "      \"name\": \"value1\",\n" +
                    "      \"type\": \"string\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"value2\",\n" +
                    "      \"type\": \"string\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"value4\",\n" +
                    "      \"type\": \"string\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"value3\",\n" +
                    "      \"type\": \"int\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
        )


        private val complexObject: ComplexObject = ComplexObject("foo", "bar", 42)


        private fun genericRecordToByteArray(genericRecord: GenericRecord): ByteArray {
            try {
                java.io.ByteArrayOutputStream().use { baos ->
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

        private fun createSerializedObject(
            payload: ByteArray,
            objectType: kotlin.String?
        ): org.axonframework.serialization.SerializedObject<ByteArray> {
            return org.axonframework.serialization.SimpleSerializedObject<ByteArray>(
                payload,
                ByteArray::class.java,
                SimpleSerializedType(objectType, null)
            )
        }

        private fun createSerializedObject(record: GenericRecord): org.axonframework.serialization.SerializedObject<GenericRecord> {
            return org.axonframework.serialization.SimpleSerializedObject<GenericRecord>(
                record,
                GenericRecord::class.java,
                SimpleSerializedType(record.getSchema().getFullName(), null)
            )
        }
    }

    fun ComplexObject.Companion.getClassSchema(): Schema = AvroKotlinSerialization().schema(ComplexObject::class).get()
}
