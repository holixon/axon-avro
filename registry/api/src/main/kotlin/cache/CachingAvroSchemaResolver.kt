package io.holixon.avro.adapter.api.cache

import io.holixon.avro.adapter.api.AvroSchemaId
import io.holixon.avro.adapter.api.AvroSchemaResolver
import io.holixon.avro.adapter.api.AvroSchemaWithId
import java.util.*

/**
 * Marks an [AvroSchemaResolver] that uses a cache to resolve a schema by its id.
 */
fun interface CachingAvroSchemaResolver : AvroSchemaResolver {

  override fun apply(schemaId: AvroSchemaId): Optional<AvroSchemaWithId>
}
