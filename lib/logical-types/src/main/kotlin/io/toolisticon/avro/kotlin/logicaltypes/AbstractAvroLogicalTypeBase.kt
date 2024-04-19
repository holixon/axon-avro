package io.toolisticon.avro.kotlin.logicaltypes

import io.toolisticon.avro.kotlin.value.LogicalTypeName
import org.apache.avro.LogicalType
import org.apache.avro.LogicalTypes
import org.apache.avro.Schema

/**
 * [T] JVM Type, like Money
 * [AVRO_TYPE] Avro Type, like CharSequence
 */
abstract class AbstractAvroLogicalTypeBase<T, AVRO_TYPE>(
  /**
   * Raw type in schema.
   */
  val schemaType: Schema.Type,
  /**
   * Logical type name.
   */
  val name: LogicalTypeName
) : AvroLogicalTypeHolder, LogicalTypes.LogicalTypeFactory {

  override val logicalType: LogicalType by lazy {
    object : LogicalType(name.value) {
      override fun validate(schema: Schema) {
        super.validate(schema)
        require(schema.type == schemaType) { "Only $schemaType is supported for logicalType=${this@AbstractAvroLogicalTypeBase.name.value}." }
      }
    }
  }

  override fun fromSchema(schema: Schema): LogicalType? = if (schema.getProp(LogicalType.LOGICAL_TYPE_PROP) == name.value) {
    logicalType
  } else {
    null
  }

  override fun getTypeName(): String = name.value
}
