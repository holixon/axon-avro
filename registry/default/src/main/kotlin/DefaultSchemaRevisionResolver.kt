package io.holixon.avro.adapter.common

import io.holixon.avro.adapter.api.AvroAdapterApi
import io.holixon.avro.adapter.api.AvroSchemaRevision
import io.holixon.avro.adapter.api.SchemaRevisionResolver
import org.apache.avro.Schema
import java.util.*

/**
 * Default schema revision resolver based on a class property.
 */
class DefaultSchemaRevisionResolver : SchemaRevisionResolver {
  private val propertyBasedResolver = AvroAdapterApi.propertyBasedSchemaRevisionResolver(AvroAdapterDefault.PROPERTY_REVISION)

  override fun apply(schema: Schema): Optional<AvroSchemaRevision> = propertyBasedResolver.apply(schema)

}
