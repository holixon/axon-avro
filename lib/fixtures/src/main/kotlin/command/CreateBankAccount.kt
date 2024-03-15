package bankaccount.command

import kotlinx.serialization.Serializable

@Serializable
data class CreateBankAccount(
  val accountId: String,
  val initialBalance: Int
) {
}
