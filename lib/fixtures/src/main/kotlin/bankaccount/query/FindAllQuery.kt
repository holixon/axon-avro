package bankaccount.query

import kotlinx.serialization.Serializable

@Serializable
data class FindAllQuery(val max:Int?=null) {
  companion object {
    @JvmStatic
    val INSTANCE = FindAllQuery()
  }
}
