package bankaccount.query

import com.github.avrokotlin.avro4k.AvroProp
import kotlinx.serialization.Serializable

@Serializable
data class CurrentBalanceQuery(
  val accountId: String
)
