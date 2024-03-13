package io.holixon.axon.avro.serializer

import com.github.avrokotlin.avro4k.Avro
import io.holixon.axon.avro.serializer.converter.*
import io.toolisticon.avro.kotlin.AvroKotlin
import io.toolisticon.avro.kotlin.AvroSchemaResolver
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.apache.avro.generic.GenericData
import org.apache.avro.util.ClassUtils
import org.axonframework.serialization.*


class AvroSerializer private constructor(
  private val converter: Converter,
  private val revisionResolver: RevisionResolver,
  private val serializationStrategies: List<AvroSerializationStrategy>,
  private val deserializationStrategies: List<AvroDeserializationStrategy>,
  private val schemaResolver: AvroSchemaResolver
) : Serializer {


  companion object {

    @JvmStatic
    fun builder() = Builder()

    operator fun invoke(builder: Builder): AvroSerializer {
      val converter = if (builder.converter is ChainingConverter) {
        (builder.converter as ChainingConverter).apply {
          registerConverter(ByteArrayToSingleObjectEncodedConverter())
          registerConverter(GenericRecordToSingleObjectEncodedConverter())
          registerConverter(SingleObjectEncodedToByteArrayConverter())
          registerConverter(GenericRecordToJsonStringConverter())
          registerConverter(SingleObjectEncodedToGenericRecordConverter(builder._avroSchemaResolver))

          builder.contentTypeConverters.forEach { this.registerConverter(it) }
        }
      } else {
        builder.converter
      }

      return AvroSerializer(
        converter = converter,
        revisionResolver = builder.revisionResolver,
        schemaResolver = builder._avroSchemaResolver,
        serializationStrategies = listOf(
          KotlinxDataClassSerializationStrategy(
            avro4k = builder.avro4k,
            genericData = AvroKotlin.defaultLogicalTypeConversions.genericData
          ),
          SpecificRecordBaseSerializationStrategy()
        ),
        deserializationStrategies = listOf(
          KotlinxDataClassDeserializationStrategy(avro4k = builder.avro4k),
          SpecificRecordBaseDeserializationStrategy()
        )
      )
    }
  }

  class Builder {
    internal lateinit var _avroSchemaResolver: AvroSchemaResolver
    internal var converter: Converter = ChainingConverter()
    internal var revisionResolver: RevisionResolver = AnnotationRevisionResolver()
    internal val contentTypeConverters: MutableList<ContentTypeConverter<*, *>> = mutableListOf()
    internal var avro4k = Avro.default

    fun avroSchemaResolver(avroSchemaResolver: AvroSchemaResolver) = apply {
      _avroSchemaResolver = avroSchemaResolver
    }

    fun addContentTypeConverter(contentTypeConverter: ContentTypeConverter<*, *>) = apply {
      this.contentTypeConverters.add(contentTypeConverter)
    }

    fun avro4k(avro4k: Avro) = apply {
      this.avro4k = avro4k
    }

    fun build(): AvroSerializer {
      require(this::_avroSchemaResolver.isInitialized) { "AvroSchemaResolver must be provided." }

      return AvroSerializer(this)
    }
  }


  override fun <T : Any> serialize(data: Any?, expectedRepresentation: Class<T>): SerializedObject<T> {
    requireNotNull(data) { "Can't serialize null." }

    // FIXME serialize MetaData
    val genericRecord = checkNotNull(serializationStrategies.firstOrNull { it.canSerialize(data::class.java) }) {
      "Could not find a matching serialization strategy for ${data::class.java}."
    }.serialize(data)

    val serializedContent: T = when (expectedRepresentation) {
      GenericData.Record::class.java -> genericRecord as T
      else -> converter.convert(genericRecord, expectedRepresentation)
    }

    return SimpleSerializedObject(
      serializedContent,
      expectedRepresentation,
      SimpleSerializedType(data::class.java.canonicalName, revisionResolver.revisionOf(data::class.java))
    )
  }

  override fun <S : Any, T : Any> deserialize(serializedObject: SerializedObject<S>): T {
    val serializedType = classForType(serializedObject.type)

    val genericRecord = converter.convert(serializedObject, GenericData.Record::class.java)

    val deserializationStrategy = checkNotNull( deserializationStrategies.firstOrNull { it.canDeserialize(serializedType) }) {
      "Could not find matching deserialization strategy for ${serializedObject.type.name}."
    }

    return deserializationStrategy.deserialize(serializedType, genericRecord.data)
  }

  override fun <T : Any> canSerializeTo(expectedRepresentation: Class<T>): Boolean {
    return GenericData.Record::class.java == expectedRepresentation
      || String::class.java == expectedRepresentation
      || converter.canConvert(SingleObjectEncodedBytes::class.java, expectedRepresentation)
  }


  override fun classForType(type: SerializedType): Class<*> {
    // TODO for  static classes inside object, java classforName cannot find the class
    return if (SimpleSerializedType.emptyType() == type) {
      Void::class.java
    } else {
      try {
        // if the class can not be found, it is unknown
        ClassUtils.forName(type.name)
      } catch (e: ClassNotFoundException) {
        UnknownSerializedType::class.java
      }
    }
  }


  override fun typeForClass(type: Class<*>?): SerializedType {
    return if (type == null || Void.TYPE == type || Void::class.java == type) {
      SimpleSerializedType.emptyType()
    } else {
      SimpleSerializedType(type.name, revisionResolver.revisionOf(type))
    }
  }

  override fun getConverter(): Converter = converter
}
