package bankaccount.conversions

import org.apache.avro.LogicalType

interface AvroLogicalTypeHolder {
  /**
   * Logical type.
   */
  val logicalType: LogicalType
}
