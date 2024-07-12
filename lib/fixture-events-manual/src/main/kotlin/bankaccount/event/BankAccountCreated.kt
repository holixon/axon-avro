package bankaccount.event

import bankaccount.conversions.MoneyLogicalType
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money

@Serializable
data class BankAccountCreated(
  val accountId: String,
  @Serializable(with = MoneyLogicalType.MoneySerializer::class)
  val initialBalance: Money
) {
  companion object {
    @JvmStatic
    fun newBuilder(): Builder = Builder()
  }

  class Builder {

    private lateinit var accountIdP: String
    private var initialBalanceP: Money = Money.of(0, "EUR")

    fun build() = BankAccountCreated(
      accountId = accountIdP,
      initialBalance = initialBalanceP
    )

    fun setAccountId(accountId: String): Builder {
      this.accountIdP = accountId
      return this
    }

    fun setInitialBalance(balance: Money): Builder {
      this.initialBalanceP = balance
      return this
    }
  }
}
