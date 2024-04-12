package bankaccount.command

import bankaccount.conversions.MoneySerializer
import kotlinx.serialization.Serializable
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.javamoney.moneta.Money

@Serializable
data class DepositMoney(
  @TargetAggregateIdentifier
  val accountId: String,
  @Serializable(with = MoneySerializer::class)
  val amount: Money
)
