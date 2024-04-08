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

@Aggregate
class BankAccount {
  @AggregateIdentifier
  protected var accountId: String? = null

  protected var balance: Int = 0

  @CommandHandler
  fun handle(cmd: WithdrawMoney) {
    if (cmd.amount > balance) {
      throw IllegalStateException(String.format("Unsufficient Balance: %d, cmd=%s", balance, cmd))
    }
    if (cmd.amount <= 0) {
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
    if (cmd.amount <= 0) {
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
    this.accountId = evt.getAccountId()
    this.balance = evt.getInitialBalance()
  }

  @EventSourcingHandler
  fun on(evt: MoneyDeposited) {
    this.accountId = evt.getAccountId()
    this.balance += evt.getAmount()
  }

  @EventSourcingHandler
  fun on(evt: MoneyWithdrawn) {
    this.accountId = evt.getAccountId()
    this.balance -= evt.getAmount()
  }

  companion object {
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
}
