package bankaccount

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import bankaccount.event.BankAccountCreated
import bankaccount.event.MoneyDeposited
import bankaccount.event.MoneyWithdrawn
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.javamoney.moneta.Money

@Aggregate
class BankAccount {

  companion object {

    private val ZERO = Money.of(0, "EUR")

    @JvmStatic
    @CommandHandler
    fun handle(cmd: CreateBankAccount): BankAccount {
      AggregateLifecycle.apply(
        BankAccountCreated.newBuilder()
          .setAccountId(cmd.accountId)
          .setInitialBalance(cmd.initialBalance)
          .build()
      )
      return BankAccount()
    }
  }

  @AggregateIdentifier
  protected var accountId: String? = null
  protected var balance: Money = ZERO

  @CommandHandler
  fun handle(cmd: WithdrawMoney) {
    require(cmd.amount.currency == balance.currency) { "This bank account only supports ${balance.currency}, but a withdraw currency was ${cmd.amount.currency}" }

    if (cmd.amount > balance) {
      throw IllegalStateException(String.format("Insufficient Balance: %d, cmd=%s", balance, cmd))
    }
    if (cmd.amount.isLessThanOrEqualTo(ZERO)) {
      throw IllegalArgumentException("Amount has to be > 0, cmd=$cmd")
    }
    AggregateLifecycle.apply(
      MoneyWithdrawn.newBuilder()
        .setAccountId(cmd.accountId)
        .setAmount(cmd.amount)
        .build()
    )
  }

  @CommandHandler
  fun handle(cmd: DepositMoney) {
    require(cmd.amount.currency == balance.currency) { "This bank account only supports ${balance.currency}, but a withdraw currency was ${cmd.amount.currency}" }
    if (cmd.amount.isLessThanOrEqualTo(ZERO)) {
      throw IllegalArgumentException("Amount has to be > 0, cmd=$cmd")
    }
    AggregateLifecycle.apply(
      MoneyDeposited.newBuilder()
        .setAccountId(cmd.accountId)
        .setAmount(cmd.amount)
        .build()
    )
  }

  @EventSourcingHandler
  fun on(evt: BankAccountCreated) {
    this.accountId = evt.accountId
    this.balance = evt.initialBalance
  }

  @EventSourcingHandler
  fun on(evt: MoneyDeposited) {
    this.accountId = evt.accountId
    this.balance = this.balance.add(evt.amount)
  }

  @EventSourcingHandler
  fun on(evt: MoneyWithdrawn) {
    this.accountId = evt.accountId
    this.balance = this.balance.subtract(evt.amount)
  }
}
