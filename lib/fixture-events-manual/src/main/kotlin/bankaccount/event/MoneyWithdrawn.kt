package bankaccount.event

import kotlinx.serialization.Serializable

@Serializable
data class MoneyWithdrawn(
  val accountId: String,
  val amount: Int
) {
  companion object {
    @JvmStatic
    fun newBuilder(): Builder = Builder()
  }

  class Builder {

    private lateinit var accountIdP: String
    private var amountP: Int = 0

    fun build() = MoneyWithdrawn(
      accountId = accountIdP,
      amount = amountP
    )

    fun setAccountId(accountId: String): Builder {
      this.accountIdP = accountId
      return this
    }

    fun setAmount(amount: Int): Builder {
      this.amountP = amount
      return this
    }
  }
}
