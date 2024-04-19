package bankaccount.conversions

import io.toolisticon.avro.kotlin.logicaltypes.AbstractAvroLogicalTypeBase
import io.toolisticon.avro.kotlin.value.LogicalTypeName
import org.apache.avro.Schema
import org.javamoney.moneta.Money

/**
 * Type factory for Money type from moneta library. Registered via SPI.
 */
class MoneyLogicalTypeFactory : AbstractAvroLogicalTypeBase<Money, CharSequence>(
  schemaType = Schema.Type.STRING,
  name = LogicalTypeName("money")
)

