package io.holixon.avro.adapter.common.registry

import io.holixon.avro.adapter.api.*
import java.util.*

/**
 * Composite Avro registry allowing registry composition.
 */
class CompositeAvroSchemaReadOnlyRegistry(
  private val registries: List<AvroSchemaReadOnlyRegistry>
) : AvroSchemaReadOnlyRegistry {

  /**
   * Using registries or read-only registries as varargs.
   */
  constructor(vararg registry: AvroSchemaRegistry) : this(registry.asList())

  /**
   * Using registries or read-only registries as varargs.
   */
  constructor(vararg registry: AvroSchemaReadOnlyRegistry) : this(registry.asList())

  init {
    require(registries.isNotEmpty()) { "Composite Avro Schema Registry must contain at least one registry." }
  }

  override fun findById(schemaId: AvroSchemaId): Optional<AvroSchemaWithId> {
    for (registry in registries) {
      val result = registry.findById(schemaId)
      if (result.isPresent) {
        return result
      }
    }
    return Optional.empty()
  }

  override fun findByInfo(info: AvroSchemaInfo): Optional<AvroSchemaWithId> {
    for (registry in registries) {
      val result = registry.findByInfo(info)
      if (result.isPresent) {
        return result
      }
    }
    return Optional.empty()
  }

  override fun findAllByCanonicalName(namespace: String, name: String): List<AvroSchemaWithId> = registries.flatMap {
    it.findAllByCanonicalName(namespace, name)
  }.distinct()

  override fun findAll(): List<AvroSchemaWithId> = registries.flatMap { it.findAll() }.distinct()

}
