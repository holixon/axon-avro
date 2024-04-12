package bankaccount.projection

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
import java.time.Instant
import java.util.*
import java.util.Objects.requireNonNull
import java.util.concurrent.ConcurrentHashMap

class CurrentBalanceProjection {
  private val accounts: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
  private val auditEvents: ConcurrentHashMap<String, MutableList<BankAccountAuditEvent>> = ConcurrentHashMap()

  companion object : KLogging()

  @EventHandler
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
  fun on(
    evt: MoneyWithdrawn,
    @SequenceNumber sequenceNumber: Long,
    @Timestamp timestamp: Instant,
    @MetaDataValue("traceId") traceId: String,
    @MetaDataValue("correlationId") correlationId: String
  ) {
    newBalance(evt.accountId, -evt.amount)

    addAuditEvent(sequenceNumber, timestamp, evt.accountId, evt.amount, traceId, correlationId)
  }

  @EventHandler
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
  fun findCurrentBalanceById(query: CurrentBalanceQuery): CurrentBalanceResult {
    val balance: Int? = accounts[query.accountId]

    val result: CurrentBalance? = if ((balance != null)
    ) CurrentBalance(query.accountId, balance)
    else null

    return CurrentBalanceResult(result)
  }

  @Suppress("UNUSED_PARAMETER")
  @QueryHandler
  fun findAll(query: FindAllQuery?): CurrentBalanceResultList {
    return CurrentBalanceResultList(
      accounts.map { it -> CurrentBalance(it.key, it.value) }.toList()
    )
  }

  @QueryHandler
  fun findAuditEventsByAccountId(query: FindBankAccountAuditEventByAccountId): BankAccountAuditEvents {
    val list = auditEvents.getOrDefault(query.accountId, emptyList())
    return BankAccountAuditEvents(list)
  }

  private fun addAuditEvent(
    sequenceNumber: Long,
    timestamp: Instant,
    accountId: String,
    amount: Int,
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

    if (!auditEvents.containsKey(accountId)) {
      auditEvents[accountId] = ArrayList<BankAccountAuditEvent>()
    }

    auditEvents[accountId]!!.add(evt)

    logger.info("received: {}", evt)
  }


  private fun newBalance(accountId: String, amount: Int) {
    accounts.compute(accountId) { _, v -> requireNotNull(v).plus(amount) }
    logger.info("Changing Balance: {} - {}", accountId, amount)
    reportAccounts()
  }

  private fun reportAccounts() {
    logger.info("ACCOUNTS:")
    logger.info(accounts.toString())
  }


  class BankAccountAuditEvent(
    private val sequenceNumber: Long,
    private val timestamp: Instant,
    val accountId: String,
    val amount: Int,
    private val traceId: String,
    private val correlationId: String
  ) {

    fun getTimestamp(): Instant {
      return timestamp
    }

    override fun equals(other: Any?): Boolean {
      if (other == null) return false
      if (this === other) {
        return true
      }
      if (this::class.java !== other::class.java) {
        return false
      }
      val that = other as BankAccountAuditEvent
      return (
        sequenceNumber == that.sequenceNumber
          && timestamp == that.timestamp
          && accountId == that.accountId
          && amount == that.amount
          && traceId == that.traceId
          && correlationId == that.correlationId
        )
    }

    @Override
    override fun hashCode(): Int {
      return Objects.hash(sequenceNumber, timestamp, accountId, amount, traceId, correlationId)
    }

    @Override
    override fun toString(): String {
      return "BankAccountAuditEvent{" +
        "sequenceNumber=" + sequenceNumber +
        ", timestamp=" + timestamp +
        ", accountId='" + accountId + '\'' +
        ", amount='" + amount + '\'' +
        ", traceId='" + traceId + '\'' +
        ", correlationId='" + correlationId + '\'' +
        '}'
    }
  }
}
