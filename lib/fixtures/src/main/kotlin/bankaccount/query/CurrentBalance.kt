package bankaccount.query

import kotlinx.serialization.Serializable

@Serializable
data class CurrentBalance(
  val accountId : String,
  val balance: Int,
)
