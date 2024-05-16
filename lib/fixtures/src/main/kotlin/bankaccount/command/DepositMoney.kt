package bankaccount.command

import bankaccount.conversions.MoneyLogicalType
import kotlinx.serialization.Serializable
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.javamoney.moneta.Money

@Serializable
data class DepositMoney(
  @TargetAggregateIdentifier
  val accountId: String,
  @Serializable(with = MoneyLogicalType.MoneySerializer::class)
  val amount: Money
)
