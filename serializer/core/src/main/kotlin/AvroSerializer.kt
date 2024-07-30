package io.holixon.axon.avro.serializer

import io.holixon.axon.avro.serializer.converter.*
import io.holixon.axon.avro.serializer.strategy.InstanceResponseTypeStrategy
import io.holixon.axon.avro.serializer.strategy.MetaDataStrategy
import io.holixon.axon.avro.serializer.strategy.MultipleInstancesResponseTypeStrategy
import io.toolisticon.kotlin.avro.AvroKotlin
import io.toolisticon.kotlin.avro.repository.AvroSchemaResolverMap
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import io.toolisticon.kotlin.avro.serialization.strategy.GenericRecordSerializationStrategy
import io.toolisticon.kotlin.avro.serialization.strategy.KotlinxDataClassStrategy
import io.toolisticon.kotlin.avro.serialization.strategy.KotlinxEnumClassStrategy
import io.toolisticon.kotlin.avro.serialization.strategy.SpecificRecordBaseStrategy
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
  private val serializationStrategies: List<GenericRecordSerializationStrategy>,
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
      axonSchemaResolver.values.forEach(builder.avroKotlinSerialization::registerSchema)

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
          registerConverter(SingleObjectEncodedToGenericRecordConverter(builder.avroKotlinSerialization))

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
      )
    }
  }

  class Builder {
    internal var avroKotlinSerialization = AvroKotlinSerialization()
    internal var converter: Converter = ChainingConverter()
    internal var revisionResolver: RevisionResolver = AnnotationRevisionResolver()
    internal val contentTypeConverters: MutableList<ContentTypeConverter<*, *>> = mutableListOf()
    internal var genericDataSupplier: Supplier<GenericData> = Supplier { AvroKotlin.genericData }

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
      return AvroSerializer(this)
    }
  }

  override fun <T : Any> serialize(data: Any?, expectedRepresentation: Class<T>): SerializedObject<T> {
    requireNotNull(data) {
      "Can't serialize null."
    }

    val strategy = serializationStrategies.firstOrNull { it.test(data::class) }.also {
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

    val strategy = serializationStrategies.firstOrNull { it.test(serializedType.kotlin) }.also {
      if (it != null) {
        logger.debug { "Using strategy ${it::class.java.name} for $serializedType." }
      } else {
        logger.debug { "Failed to detect strategy, will pass $serializedType to converter chain." }
      }
    }

    @Suppress("UNCHECKED_CAST", "IfThenToElvis")
    return if (strategy != null) {
      strategy.deserialize(
        serializedType = serializedType.kotlin,
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
