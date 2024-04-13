package bankaccount.command

import bankaccount.conversions.MoneyLogicalType
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money

@Serializable
data class CreateBankAccount(
  val accountId: String,
  @Serializable(with = MoneyLogicalType.Serializer::class)
  val initialBalance: Money
) {
}
