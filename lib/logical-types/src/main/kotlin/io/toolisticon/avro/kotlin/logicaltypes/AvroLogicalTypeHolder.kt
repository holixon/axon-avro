package io.toolisticon.avro.kotlin.logicaltypes

import org.apache.avro.LogicalType

interface AvroLogicalTypeHolder {
  /**
   * Logical type.
   */
  val logicalType: LogicalType
}
