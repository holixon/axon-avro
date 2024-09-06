package holi.bank

import holi.bank.BankAccountContextCommandHandlers.BankAccountAggregateCommandHandlers
import holi.bank.BankAccountContextCommandHandlers.BankAccountAggregateCommandHandlers.BankAccountAggregateFactory
import holi.bank.BankAccountContextEventSourcingHandlers.BankAccountAggregateSourcingHandlers
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate


@Aggregate
class BankAccountAggregate() : BankAccountAggregateCommandHandlers, BankAccountAggregateSourcingHandlers {

  @AggregateIdentifier
  internal lateinit var accountId: String
  internal var balance: Int = -1

  companion object : BankAccountAggregateFactory {

    private const val INITIAL_BALANCE_MIN = 20

    @JvmStatic
    @CommandHandler // need to duplicate command handler
    @Throws(IllegalInitialBalance::class)
    override fun createBankAccount(command: CreateBankAccountCommand): BankAccountAggregate {
      if (command.initialBalance < INITIAL_BALANCE_MIN) {
        throw IllegalInitialBalance("Initial balance of the account must exceed ${INITIAL_BALANCE_MIN}, but it was ${command.initialBalance}.")
      }
      AggregateLifecycle.apply(BankAccountCreatedEvent(command.accountId, command.initialBalance))

      return BankAccountAggregate()
    }

  }

  override fun depositMoney(command: DepositMoneyCommand) {
    AggregateLifecycle.apply(MoneyDepositedEvent(this.accountId, command.amount))
  }

  override fun withdrawMoney(command: WithdrawMoneyCommand) {
    if (this.balance >= command.amount) {
      AggregateLifecycle.apply(MoneyWithdrawnEvent(this.accountId, command.amount))
    }
  }

  override fun onBankAccountCreatedEvent(event: BankAccountCreatedEvent) {
    this.accountId = event.accountId
    this.balance = event.initialBalance
  }

  override fun onMoneyDepositedEvent(event: MoneyDepositedEvent) {
    this.balance += event.amount
  }

  override fun onMoneyWithdrawnEvent(event: MoneyWithdrawnEvent) {
    this.balance -= event.amount
  }
}
