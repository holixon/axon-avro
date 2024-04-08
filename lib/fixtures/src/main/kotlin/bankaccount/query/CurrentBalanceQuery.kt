package bankaccount.query

import kotlinx.serialization.Serializable

@Serializable
data class CurrentBalanceQuery(
  val accountId: String
)
