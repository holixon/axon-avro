package bankaccount.query

import kotlinx.serialization.Serializable

@Serializable
data class CurrentBalanceResultList(
  private val results: List<CurrentBalance>
) : List<CurrentBalance> by results
