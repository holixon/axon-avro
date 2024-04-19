package bankaccount.conversions

import io.toolisticon.avro.kotlin.value.LogicalTypeName
import org.apache.avro.Schema
import org.javamoney.moneta.Money

class MoneyLogicalTypeFactory : AbstractAvroLogicalTypeBase<Money, CharSequence>(
  schemaType = Schema.Type.STRING,
  name = LogicalTypeName("money")
)

