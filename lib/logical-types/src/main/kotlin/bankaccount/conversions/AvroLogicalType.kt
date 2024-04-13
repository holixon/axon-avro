package bankaccount.conversions

import org.apache.avro.LogicalType
import org.apache.avro.LogicalTypes.LogicalTypeFactory

interface AvroLogicalType<T, AVRO4K_TYPE> {
  /**
   * Logical type.
   */
  val logicalType: LogicalType

  fun toAvro(value: T): AVRO4K_TYPE

  fun toJvm(value: AVRO4K_TYPE): T
}
