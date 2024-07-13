package bankaccount.projection

import bankaccount.event.BankAccountAuditEvent
import bankaccount.event.BankAccountCreated
import bankaccount.event.MoneyDeposited
import bankaccount.event.MoneyWithdrawn
import bankaccount.query.*
import bankaccount.query.BankAccountAuditQuery.BankAccountAuditEvents
import bankaccount.query.BankAccountAuditQuery.FindBankAccountAuditEventByAccountId
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.eventhandling.Timestamp
import org.axonframework.messaging.annotation.MetaDataValue
import org.axonframework.queryhandling.QueryHandler
import org.javamoney.moneta.Money
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class CurrentBalanceProjection {

  companion object : KLogging()

  private val accounts: ConcurrentHashMap<String, Money> = ConcurrentHashMap()
  private val auditEvents: ConcurrentHashMap<String, MutableList<BankAccountAuditEvent>> = ConcurrentHashMap()

  @EventHandler
  @Suppress("unused")
  fun on(
    evt: BankAccountCreated,
    @SequenceNumber sequenceNumber: Long,
    @Timestamp timestamp: Instant,
    @MetaDataValue("traceId") traceId: String,
    @MetaDataValue("correlationId") correlationId: String
  ) {
    accounts[evt.accountId] = evt.initialBalance
    reportAccounts()
    addAuditEvent(sequenceNumber, timestamp, evt.accountId, evt.initialBalance, traceId, correlationId)
  }

  @EventHandler
  @Suppress("unused")
  fun on(
    evt: MoneyWithdrawn,
    @SequenceNumber sequenceNumber: Long,
    @Timestamp timestamp: Instant,
    @MetaDataValue("traceId") traceId: String,
    @MetaDataValue("correlationId") correlationId: String
  ) {
    newBalance(evt.accountId, evt.amount.negate())
    addAuditEvent(sequenceNumber, timestamp, evt.accountId, evt.amount, traceId, correlationId)
  }

  @EventHandler
  @Suppress("unused")
  fun on(
    evt: MoneyDeposited,
    @SequenceNumber sequenceNumber: Long,
    @Timestamp timestamp: Instant,
    @MetaDataValue("traceId") traceId: String,
    @MetaDataValue("correlationId") correlationId: String
  ) {
    newBalance(evt.accountId, evt.amount)
    addAuditEvent(sequenceNumber, timestamp, evt.accountId, evt.amount, traceId, correlationId)
  }

  @QueryHandler
  @Suppress("unused")
  fun findCurrentBalanceById(query: CurrentBalanceQuery): CurrentBalanceResult {
    val balance = accounts[query.accountId]

    val result: CurrentBalance? = if ((balance != null)) {
      CurrentBalance(query.accountId, balance)
    } else {
      null
    }

    return CurrentBalanceResult(result)
  }

  @Suppress("UNUSED_PARAMETER", "unused")
  @QueryHandler
  fun findAll(query: FindAllQuery?): CurrentBalanceResultList {
    return CurrentBalanceResultList(
      accounts.map { CurrentBalance(it.key, it.value) }.toList()
    )
  }

  @QueryHandler
  @Suppress("unused")
  fun findAuditEventsByAccountId(query: FindBankAccountAuditEventByAccountId): BankAccountAuditEvents {
    val list = auditEvents.getOrDefault(query.accountId, emptyList())
    return BankAccountAuditEvents(list)
  }

  private fun addAuditEvent(
    sequenceNumber: Long,
    timestamp: Instant,
    accountId: String,
    amount: Money,
    traceId: String,
    correlationId: String
  ) {
    val evt = BankAccountAuditEvent(
      sequenceNumber,
      timestamp,
      accountId,
      amount,
      traceId,
      correlationId
    )

    auditEvents.computeIfAbsent(accountId) { _ -> mutableListOf() }.add(evt)
    logger.info("received: {}", evt)
  }

  private fun newBalance(accountId: String, amount: Money) {
    accounts.compute(accountId) { _, v -> requireNotNull(v).add(amount) }
    logger.info("Changing Balance: {} - {}", accountId, amount)
    reportAccounts()
  }

  private fun reportAccounts() {
    logger.info("ACCOUNTS:")
    logger.info("\t $accounts")
  }
}
