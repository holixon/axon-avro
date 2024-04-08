package io.holixon.axon.avro.serializer

import com.github.avrokotlin.avro4k.Avro
import io.holixon.axon.avro.serializer.converter.*
import io.holixon.axon.avro.serializer.strategy.*
import io.toolisticon.avro.kotlin.AvroKotlin
import io.toolisticon.avro.kotlin.AvroSchemaResolver
import io.toolisticon.avro.kotlin.AvroSchemaResolverMap
import io.toolisticon.avro.kotlin.plus
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import mu.KLogging
import org.apache.avro.generic.GenericData
import org.apache.avro.util.ClassUtils
import org.axonframework.serialization.*
import java.util.function.Supplier


class AvroSerializer private constructor(
  private val converter: Converter,
  private val revisionResolver: RevisionResolver,
  private val serializationStrategies: List<AvroSerializationStrategy>,
  private val deserializationStrategies: List<AvroDeserializationStrategy>,
  private val schemaResolver: AvroSchemaResolver
) : Serializer {


  companion object : KLogging() {

    private val axonSchemaResolver: AvroSchemaResolverMap = AvroSchemaResolverMap(
      listOf(
        MetaDataStrategy.SCHEMA,
        InstanceResponseTypeStrategy.SCHEMA,
        MultipleInstancesResponseTypeStrategy.SCHEMA,
      ).associateBy { it.fingerprint }
    )

    @JvmStatic
    fun builder() = Builder()

    operator fun invoke(builder: Builder): AvroSerializer {

      val schemaResolver = if (builder._avroSchemaResolver is AvroSchemaResolverMap) {
        builder._avroSchemaResolver + axonSchemaResolver
      } else {
        builder._avroSchemaResolver + axonSchemaResolver
      }

      val converter = if (builder.converter is ChainingConverter) {
        (builder.converter as ChainingConverter).apply {
          registerConverter(ByteArrayToSingleObjectEncodedConverter())
          registerConverter(GenericRecordToSingleObjectEncodedConverter())
          registerConverter(SingleObjectEncodedToByteArrayConverter())
          registerConverter(GenericRecordToJsonStringConverter())
          registerConverter(SingleObjectEncodedToGenericRecordConverter(schemaResolver))
          registerConverter(JsonStringToStringConverter())

          builder.contentTypeConverters.forEach { this.registerConverter(it) }
        }
      } else {
        builder.converter
      }

      val genericData = builder.genericDataSupplier.get()

      val kotlinxDataClassStrategy = KotlinxDataClassStrategy(
        avro4k = builder.avro4k,
        genericData = genericData
      )
      val kotlinxEnumClassStrategy = KotlinxEnumClassStrategy(
        avro4k = builder.avro4k,
        genericData = genericData
      )
      val specificRecordBaseStrategy = SpecificRecordBaseStrategy()
      val metaDataStrategy = MetaDataStrategy(genericData = genericData)
      val instanceResponseTypeStrategy = InstanceResponseTypeStrategy(genericData)
      val multipleInstancesResponseTypeStrategy = MultipleInstancesResponseTypeStrategy(genericData)


      return AvroSerializer(
        converter = converter,
        revisionResolver = builder.revisionResolver,
        schemaResolver = schemaResolver,
        serializationStrategies = listOf(
          metaDataStrategy,
          instanceResponseTypeStrategy,
          multipleInstancesResponseTypeStrategy,
          kotlinxDataClassStrategy,
          kotlinxEnumClassStrategy,
          specificRecordBaseStrategy
        ),
        deserializationStrategies = listOf(
          metaDataStrategy,
          kotlinxDataClassStrategy,
          kotlinxEnumClassStrategy,
          instanceResponseTypeStrategy,
          multipleInstancesResponseTypeStrategy,
          specificRecordBaseStrategy
        ),
      )
    }
  }

  class Builder {
    internal lateinit var _avroSchemaResolver: AvroSchemaResolver
    internal var converter: Converter = ChainingConverter()
    internal var revisionResolver: RevisionResolver = AnnotationRevisionResolver()
    internal val contentTypeConverters: MutableList<ContentTypeConverter<*, *>> = mutableListOf()
    internal var avro4k = Avro.default
    internal var genericDataSupplier: Supplier<GenericData> = Supplier { AvroKotlin.defaultLogicalTypeConversions.genericData }

    fun avroSchemaResolver(avroSchemaResolver: AvroSchemaResolver) = apply {
      _avroSchemaResolver = avroSchemaResolver
    }

    fun addContentTypeConverter(contentTypeConverter: ContentTypeConverter<*, *>) = apply {
      this.contentTypeConverters.add(contentTypeConverter)
    }

    fun avro4k(avro4k: Avro) = apply {
      this.avro4k = avro4k
    }

    fun genericDataSupplier(supplier: Supplier<GenericData>) = apply {
      this.genericDataSupplier = supplier
    }

    fun build(): AvroSerializer {
      require(this::_avroSchemaResolver.isInitialized) { "AvroSchemaResolver must be provided." }

      return AvroSerializer(this)
    }
  }


  override fun <T : Any> serialize(data: Any?, expectedRepresentation: Class<T>): SerializedObject<T> {
    requireNotNull(data) {
      "Can't serialize null."
    }

    val strategy = serializationStrategies.firstOrNull { it.canSerialize(data::class.java) }

    val serializedContent: T = if (strategy != null) {
      val genericRecord = strategy.serialize(data)
      when (expectedRepresentation) {
        GenericData.Record::class.java -> genericRecord as T
        else -> converter.convert(genericRecord, expectedRepresentation)
      }
    } else {
      converter.convert(data, expectedRepresentation)
    }

    return SimpleSerializedObject(
      serializedContent,
      expectedRepresentation,
      SimpleSerializedType(data::class.java.canonicalName, revisionResolver.revisionOf(data::class.java))
    )
  }

  override fun <S : Any, T : Any> deserialize(serializedObject: SerializedObject<S>): T {
    val serializedType = classForType(serializedObject.type)

    val strategy = deserializationStrategies.firstOrNull { it.canDeserialize(serializedType) }

    return strategy?.deserialize(serializedType, converter.convert(serializedObject, GenericData.Record::class.java).data)
      ?: converter.convert(serializedObject.data, serializedType) as T
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
