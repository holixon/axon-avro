package io.holixon.avro.adapter.api

import io.holixon.avro.adapter.api.type.AvroSchemaInfoData
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificData
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * Returns a unique id for the given schema that is used to load the schema from a repository.
 */
fun interface SchemaIdSupplier : Function<Schema, AvroSchemaId>

/**
 * Returns the revision for a given schema.
 */
fun interface SchemaRevisionResolver : Function<Schema, Optional<AvroSchemaRevision>>

/**
 * Provides the (implementation specific) [AvroSchemaRevision] for a given [AvroSchemaId].
 */
fun interface AvroSchemaRevisionForSchemaIdResolver : Function<AvroSchemaId, Optional<AvroSchemaRevision>>

/**
 * Returns `true` if the [ByteBuffer] conforms to the singleObject encoding specification.
 */
fun interface IsAvroSingleObjectEncodedPredicate : Predicate<ByteBuffer>

/**
 * Global utility methods.
 */
object AvroAdapterApi {

  /**
   * Determines the revision of a given schema by reading the String value of the given object-property.
   */
  @JvmStatic
  fun propertyBasedSchemaRevisionResolver(propertyKey: String): SchemaRevisionResolver =
    SchemaRevisionResolver { schema -> Optional.ofNullable(schema.getObjectProp(propertyKey) as String?) }

  @JvmStatic
  fun schemaForClass(recordClass: Class<*>): Schema = SpecificData(recordClass.classLoader).getSchema(recordClass)

  @JvmStatic
  fun schemaForClass(recordClass: KClass<*>): Schema = schemaForClass(recordClass.java)

  @JvmStatic
  fun Schema.extractSchemaInfo(schemaRevisionResolver: SchemaRevisionResolver): AvroSchemaInfoData = AvroSchemaInfoData(
    namespace = namespace,
    name = name,
    revision = schemaRevisionResolver.apply(this).orElse(null)
  )

  /**
   * Creates a schema resolver out of a read-only registry.
   * @return [AvroSchemaResolver] derived from registry.
   */
  fun AvroSchemaReadOnlyRegistry.schemaResolver(): AvroSchemaResolver =
    AvroSchemaResolver { schemaId -> this@schemaResolver.findById(schemaId) }

}
