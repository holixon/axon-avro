package bankaccount.query

import kotlinx.serialization.Serializable

@Serializable
data class CurrentBalanceResult(
  val value: CurrentBalance?
)
