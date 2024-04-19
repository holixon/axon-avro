package bankaccount.command

import bankaccount.conversions.MoneySerializer
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money

@Serializable
data class CreateBankAccount(
  val accountId: String,
  @Serializable(with = MoneySerializer::class)
  val initialBalance: Money
) {
}
