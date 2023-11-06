package io.holixon.avro.adapter.api

import org.apache.avro.Schema
import java.util.*

/**
 * The Schema Registry is responsible for storing and retrieving arvo schema files.
 */
interface AvroSchemaRegistry : AvroSchemaReadOnlyRegistry {

  /**
   * Stores a new [Schema] (version) in the repository.
   */
  fun register(schema: Schema): AvroSchemaWithId

}

/**
 * The Schema Registry is responsible for retrieving arvo schema files.
 */
interface AvroSchemaReadOnlyRegistry {
  /**
   * Finds a stored [Schema] based on its unique [AvroSchemaId] (e.g. its fingerprint).
   */
  fun findById(schemaId: AvroSchemaId): Optional<AvroSchemaWithId>

  /**
   * Finds a stored [Schema] based on its derived info.
   */
  fun findByInfo(info: AvroSchemaInfo): Optional<AvroSchemaWithId>

  /**
   * Finds all stored [Schema]s based on their namespaces and names (e.g. FQN).
   */
  fun findAllByCanonicalName(namespace: String, name: String): List<AvroSchemaWithId>

  /**
   * Simply lists all stored [Schema]s.
   */
  fun findAll(): List<AvroSchemaWithId>

}
