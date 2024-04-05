package bankaccount.command

import com.github.avrokotlin.avro4k.AvroProp
import kotlinx.serialization.Serializable

@Serializable
data class CreateBankAccount(
  val accountId: String,
  val initialBalance: Int
) {
}
