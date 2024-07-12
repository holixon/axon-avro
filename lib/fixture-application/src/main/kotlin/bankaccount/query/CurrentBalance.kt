package bankaccount.query

import bankaccount.conversions.MoneyLogicalType.MoneySerializer
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money

@Serializable
data class CurrentBalance(
  val accountId: String,
  @Serializable(with = MoneySerializer::class)
  val balance: Money,
)
