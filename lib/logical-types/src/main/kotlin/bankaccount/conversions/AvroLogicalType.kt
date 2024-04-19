package bankaccount.conversions

import org.apache.avro.LogicalType

interface AvroLogicalType<T, AVRO4K_TYPE> {
  /**
   * Logical type.
   */
  val logicalType: LogicalType
}
