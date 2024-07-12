package io.holixon.axon.avro.serializer

import io.holixon.axon.avro.serializer.converter.*
import io.holixon.axon.avro.serializer.strategy.*
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.repository.AvroSchemaResolver
import io.toolisticon.kotlin.avro.repository.AvroSchemaResolverMap
import io.toolisticon.kotlin.avro.repository.plus
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import mu.KLogging
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.ClassUtils
import org.axonframework.serialization.*
import java.util.function.Supplier


class AvroSerializer private constructor(
  private val converter: Converter,
  private val revisionResolver: RevisionResolver,
  private val serializationStrategies: List<AvroSerializationStrategy>,
  private val deserializationStrategies: List<AvroDeserializationStrategy>
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

      val schemaResolver = if (builder.builderAvroSchemaResolver is AvroSchemaResolverMap) {
        builder.builderAvroSchemaResolver + axonSchemaResolver
      } else {
        builder.builderAvroSchemaResolver + axonSchemaResolver
      }

      val converter = if (builder.converter is ChainingConverter) {
        logger.debug { "" }
        (builder.converter as ChainingConverter).apply {

          /* FIXME -> CRAZY JavaDoc for registerConverter


          Registers the given converter with this factory.
          The converter which is registered last will be inspected first when finding
          a suitable converter for a given input and output type. An alternative to
          explicit converter registration (but without the ordering guarantees) is to create
          a file called org. axonframework. serialization. ContentTypeConverter in / META-INF/ services/ on
          the class path which contains the fully qualified class names of the converters,
          separated by newlines. These implementations must have a public no-arg constructor.

          Params:
          converter – the converter to register.
          */

          registerConverter(SingleObjectEncodedToByteArrayConverter())
          registerConverter(GenericRecordToSingleObjectEncodedConverter())
          registerConverter(GenericDataRecordToSingleObjectEncodedConverter())
          registerConverter(ListRecordToJsonStringConverter())
          registerConverter(ListRecordToSingleObjectEncodedConverter())

          registerConverter(ByteArrayToSingleObjectEncodedConverter())
          registerConverter(SingleObjectEncodedToGenericRecordConverter(schemaResolver))

          // JSON handling in inverted order: GenericRecord -> String
          registerConverter(JsonStringToStringConverter())
          registerConverter(GenericRecordToJsonStringConverter())

          builder.contentTypeConverters.forEach { this.registerConverter(it) }
        }
      } else {
        builder.converter
      }

      val genericData = builder.genericDataSupplier.get()

      val kotlinxDataClassStrategy = KotlinxDataClassStrategy(
        avroKotlinSerialization = builder.avroKotlinSerialization
      )
      val kotlinxEnumClassStrategy = KotlinxEnumClassStrategy(
        avroKotlinSerialization = builder.avroKotlinSerialization
      )
      val specificRecordBaseStrategy = SpecificRecordBaseStrategy()
      val metaDataStrategy = MetaDataStrategy(genericData = genericData)
      val instanceResponseTypeStrategy = InstanceResponseTypeStrategy()
      val multipleInstancesResponseTypeStrategy = MultipleInstancesResponseTypeStrategy()


      return AvroSerializer(
        converter = converter,
        revisionResolver = builder.revisionResolver,
        serializationStrategies = listOf(
          instanceResponseTypeStrategy,
          kotlinxDataClassStrategy,
          kotlinxEnumClassStrategy,
          metaDataStrategy,
          multipleInstancesResponseTypeStrategy,
          specificRecordBaseStrategy
        ),
        deserializationStrategies = listOf(
          instanceResponseTypeStrategy,
          kotlinxDataClassStrategy,
          kotlinxEnumClassStrategy,
          metaDataStrategy,
          multipleInstancesResponseTypeStrategy,
          specificRecordBaseStrategy
        ),
      )
    }
  }

  class Builder {
    internal lateinit var builderAvroSchemaResolver: AvroSchemaResolver
    internal var converter: Converter = ChainingConverter()
    internal var revisionResolver: RevisionResolver = AnnotationRevisionResolver()
    internal val contentTypeConverters: MutableList<ContentTypeConverter<*, *>> = mutableListOf()
    internal var avroKotlinSerialization = AvroKotlinSerialization()
    internal var genericDataSupplier: Supplier<GenericData> = Supplier { AvroKotlin.genericData }

    fun avroSchemaResolver(avroSchemaResolver: AvroSchemaResolver) = apply {
      builderAvroSchemaResolver = avroSchemaResolver
    }

    fun addContentTypeConverter(contentTypeConverter: ContentTypeConverter<*, *>) = apply {
      this.contentTypeConverters.add(contentTypeConverter)
    }

    fun avroKotlinSerialization(avroKotlinSerialization: AvroKotlinSerialization) = apply {
      this.avroKotlinSerialization = avroKotlinSerialization
    }

    fun genericDataSupplier(supplier: Supplier<GenericData>) = apply {
      this.genericDataSupplier = supplier
    }

    fun build(): AvroSerializer {
      require(this::builderAvroSchemaResolver.isInitialized) { "AvroSchemaResolver must be provided." }

      return AvroSerializer(this)
    }
  }


  override fun <T : Any> serialize(data: Any?, expectedRepresentation: Class<T>): SerializedObject<T> {
    requireNotNull(data) {
      "Can't serialize null."
    }

    val strategy = serializationStrategies.firstOrNull { it.canSerialize(data::class.java) }.also {
      if (it != null) {
        logger.debug { "Using strategy ${it::class.java.name} for ${data::class.java}." }
      } else {
        logger.debug { "Failed to detect strategy, will pass ${data::class.java} to converter chain." }
      }
    }

    val serializedContent: T = if (strategy != null) {
      val genericRecord = strategy.serialize(data)
      @Suppress("UNCHECKED_CAST")
      when (expectedRepresentation) {
        GenericRecord::class.java -> genericRecord as T
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

    val strategy = deserializationStrategies.firstOrNull { it.canDeserialize(serializedType) }.also {
      if (it != null) {
        logger.debug { "Using strategy ${it::class.java.name} for $serializedType." }
      } else {
        logger.debug { "Failed to detect strategy, will pass $serializedType to converter chain." }
      }
    }

    @Suppress("UNCHECKED_CAST", "IfThenToElvis")
    return if (strategy != null) {
      strategy.deserialize(
        serializedType = serializedType,
        data = converter.convert(serializedObject, GenericRecord::class.java).data
      )
    } else {
      converter.convert(serializedObject.data, serializedType)
    } as T
  }

  override fun <T : Any> canSerializeTo(expectedRepresentation: Class<T>): Boolean {
    return GenericRecord::class.java == expectedRepresentation
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
