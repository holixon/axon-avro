package bankaccount.query

import bankaccount.conversions.MoneyLogicalType
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money

@Serializable
data class CurrentBalance(
  val accountId: String,
  @Serializable(with = MoneyLogicalType.Serializer::class)
  val balance: Money,
)
