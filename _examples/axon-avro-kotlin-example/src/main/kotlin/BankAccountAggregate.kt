package io.holixon.axon.avro.example.kotlin

import holi.bank.*
import holi.bank.BankAccountContextCommandHandlers.BankAccountAggregateCommandHandlers
import holi.bank.BankAccountContextEventSourcingHandlers.BankAccountAggregateSourcingHandlers
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy.ALWAYS
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate


@Aggregate
class BankAccountAggregate() : BankAccountAggregateCommandHandlers, BankAccountAggregateSourcingHandlers {

  @AggregateIdentifier
  internal lateinit var accountId: String
  internal var balance: Int = -1

  companion object {
    private const val INITIAL_BALANCE_MIN = 20
  }

  @CommandHandler
  @CreationPolicy(value = ALWAYS)
  @Throws(IllegalInitialBalance::class)
  override fun createBankAccount(command: CreateBankAccountCommand): String {
    if (command.initialBalance < INITIAL_BALANCE_MIN) {
      throw IllegalInitialBalance("Initial balance of the account must exceed ${INITIAL_BALANCE_MIN}, but it was ${command.initialBalance}.")
    }
    AggregateLifecycle.apply(BankAccountCreatedEvent(command.accountId, command.initialBalance))
    return command.accountId
  }

  @CommandHandler
  override fun depositMoney(command: DepositMoneyCommand) {
    AggregateLifecycle.apply(MoneyDepositedEvent(this.accountId, command.amount))
  }

  @CommandHandler
  override fun withdrawMoney(command: WithdrawMoneyCommand) {
    if (this.balance >= command.amount) {
      AggregateLifecycle.apply(MoneyWithdrawnEvent(this.accountId, command.amount))
    }
  }

  @EventSourcingHandler
  override fun onBankAccountCreatedEvent(event: BankAccountCreatedEvent) {
    this.accountId = event.accountId
    this.balance = event.initialBalance
  }

  @EventSourcingHandler
  override fun onMoneyDepositedEvent(event: MoneyDepositedEvent) {
    this.balance += event.amount
  }

  @EventSourcingHandler
  override fun onMoneyWithdrawnEvent(event: MoneyWithdrawnEvent) {
    this.balance -= event.amount
  }

}
