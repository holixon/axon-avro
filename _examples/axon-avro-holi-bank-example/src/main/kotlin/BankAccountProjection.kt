package holi.bank

import holi.bank.BankAccountContextEventHandlers.BankAccountContextAllEventHandlers
import holi.bank.BankAccountContextQueries.BankAccountProjectionQueries
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.util.Optional


@Component
class BankAccountProjection : BankAccountContextAllEventHandlers, BankAccountProjectionQueries {

  private val balances: MutableMap<String, CurrentBalance> = mutableMapOf()
  private val transfers: MutableMap<String, MutableList<MoneyTransfer>> = mutableMapOf()

  override fun findCurrentBalanceForAccountId(query: FindCurrentBalanceByAccountIdQuery): Optional<CurrentBalance> {
    return Optional.ofNullable(balances[query.accountId])
  }

  override fun findAllMoneyTransfersForAccountId(query: FindAllMoneyTransfersByAccountIdQuery): MoneyTransfers {
    return MoneyTransfers(transfers[query.accountId] ?: emptyList())
  }

  override fun onBankAccountCreatedEvent(event: BankAccountCreatedEvent) {
    balances[event.accountId] = CurrentBalance(event.accountId, event.initialBalance)
    transfers[event.accountId] = mutableListOf()
  }

  override fun onMoneyDepositedEvent(event: MoneyDepositedEvent) {
    balances.computeIfPresent(event.accountId) { _, balance -> balance.copy(balance = balance.balance + event.amount) }
    transfers.computeIfPresent(event.accountId) { _, transfer -> transfer.apply { add(MoneyTransfer(MoneyTransferType.DEPOSIT, event.amount)) } }
  }

  override fun onMoneyWithdrawnEvent(event: MoneyWithdrawnEvent) {
    balances.computeIfPresent(event.accountId) { _, balance -> balance.copy(balance = balance.balance - event.amount) }
    transfers.computeIfPresent(event.accountId) { _, transfer -> transfer.apply { add(MoneyTransfer(MoneyTransferType.WITHDRAWAL, event.amount)) } }
  }
}
