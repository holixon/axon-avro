package bankaccount.command

import kotlinx.serialization.Serializable
import org.axonframework.modelling.command.TargetAggregateIdentifier

@Serializable
data class DepositMoney(
  @TargetAggregateIdentifier
  val accountId: String,
  val amount: Int
)
