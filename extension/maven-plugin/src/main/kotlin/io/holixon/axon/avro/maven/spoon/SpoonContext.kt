package io.holixon.axon.avro.maven.spoon

import io.holixon.axon.avro.maven.avro.schemaFieldToRecordMetadata
import io.holixon.axon.avro.maven.fn.HasRuntimeDependencyPredicate
import io.holixon.axon.avro.types.meta.RecordMetaData
import mu.KLogger
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecordBase
import spoon.reflect.declaration.CtClass
import java.util.concurrent.ConcurrentHashMap

/**
 * SpoonContext provides a common cache to avoid redundant calls to schema and metaData.
 */
class SpoonContext(
  val logger: KLogger,
  val hasRuntimeDependency: HasRuntimeDependencyPredicate
) {

  private val schemas: MutableMap<CtClass<out SpecificRecordBase>, Schema> =
    ConcurrentHashMap<CtClass<out SpecificRecordBase>, Schema>()
  private val metaData: ConcurrentHashMap<CtClass<out SpecificRecordBase>, RecordMetaData> =
    ConcurrentHashMap<CtClass<out SpecificRecordBase>, RecordMetaData>()

  /**
   * Return the avro schema derived from the given class.
   */
  fun schema(type: CtClass<out SpecificRecordBase>) = schemas.computeIfAbsent(type) {
    schemaFieldToRecordMetadata(it.getField("SCHEMA\$").defaultExpression.toString())
  }

  /**
   * Return the metaData property of schema derived from given class.
   */
  fun metaData(type: CtClass<out SpecificRecordBase>) = metaData.computeIfAbsent(type) {
    val schema = schema(it)

    RecordMetaData.parse(schema)
  }
}
