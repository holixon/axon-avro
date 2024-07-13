package bankaccount.event

import bankaccount.conversions.MoneyLogicalType
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money

@Serializable
data class MoneyWithdrawn(
  val accountId: String,
  @Serializable(with = MoneyLogicalType.MoneySerializer::class)
  val amount: Money
) {
  companion object {
    @JvmStatic
    fun newBuilder(): Builder = Builder()
  }

  class Builder {

    private lateinit var accountIdP: String
    private var amountP: Money = Money.of(0, "EUR")

    fun build() = MoneyWithdrawn(
      accountId = accountIdP,
      amount = amountP
    )

    fun setAccountId(accountId: String): Builder {
      this.accountIdP = accountId
      return this
    }

    fun setAmount(amount: Money): Builder {
      this.amountP = amount
      return this
    }
  }
}
