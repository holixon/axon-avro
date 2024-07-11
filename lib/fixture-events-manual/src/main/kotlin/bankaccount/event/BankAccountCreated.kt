package bankaccount.event

data class BankAccountCreated(
  val accountId: String,
  val initialBalance: Int
) {
  companion object {
    @JvmStatic
    fun newBuilder(): Builder = Builder()
  }

  class Builder {

    private lateinit var accountIdP: String
    private var initialBalanceP: Int = 0

    fun build() = BankAccountCreated(
      accountId = accountIdP,
      initialBalance = initialBalanceP
    )

    fun setAccountId(accountId: String): Builder {
      this.accountIdP = accountId
      return this
    }

    fun setInitialBalance(balance: Int): Builder {
      this.initialBalanceP = balance
      return this
    }
  }
}
