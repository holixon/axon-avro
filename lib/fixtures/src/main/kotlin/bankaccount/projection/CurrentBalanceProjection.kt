package bankaccount.projection

import bankaccount.event.BankAccountCreated
import bankaccount.event.MoneyDeposited
import bankaccount.event.MoneyWithdrawn
import bankaccount.query.*
import bankaccount.query.BankAccountAuditQuery.BankAccountAuditEvents
import bankaccount.query.BankAccountAuditQuery.FindBankAccountAuditEventByAccountId
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.eventhandling.Timestamp
import org.axonframework.messaging.annotation.MetaDataValue
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.Objects.requireNonNull
import java.util.concurrent.ConcurrentHashMap

class CurrentBalanceProjection {
  private val accounts: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
  private val auditEvents: ConcurrentHashMap<String, MutableList<BankAccountAuditEvent>> = ConcurrentHashMap()

  @EventHandler
  fun on(
    evt: BankAccountCreated,
    @SequenceNumber sequenceNumber: Long,
    @Timestamp timestamp: Instant,
    @MetaDataValue("traceId") traceId: String,
    @MetaDataValue("correlationId") correlationId: String
  ) {
    accounts.put(evt.getAccountId(), evt.initialBalance)

    LOGGER.info("ACCOUNTS:")
    LOGGER.info(accounts.toString())

    addAuditEvent(sequenceNumber, timestamp, evt.getAccountId(), evt.getInitialBalance(), traceId, correlationId)
  }

  @EventHandler
  fun on(
    evt: MoneyWithdrawn,
    @SequenceNumber sequenceNumber: Long,
    @Timestamp timestamp: Instant,
    @MetaDataValue("traceId") traceId: String,
    @MetaDataValue("correlationId") correlationId: String
  ) {
    newBalance(evt.getAccountId(), -evt.getAmount())

    addAuditEvent(sequenceNumber, timestamp, evt.getAccountId(), evt.getAmount(), traceId, correlationId)
  }

  @EventHandler
  fun on(
    evt: MoneyDeposited,
    @SequenceNumber sequenceNumber: Long,
    @Timestamp timestamp: Instant,
    @MetaDataValue("traceId") traceId: String,
    @MetaDataValue("correlationId") correlationId: String
  ) {
    newBalance(evt.getAccountId(), evt.getAmount())

    addAuditEvent(sequenceNumber, timestamp, evt.getAccountId(), evt.getAmount(), traceId, correlationId)
  }

  @QueryHandler
  fun findCurrentBalanceById(query: CurrentBalanceQuery): CurrentBalanceResult {
    val balance: Int? = accounts[query.accountId]

    val result: CurrentBalance? = if ((balance != null)
    ) CurrentBalance(query.accountId, balance)
    else null

    return CurrentBalanceResult(result)
  }

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

    LOGGER.info("received: {}", evt)
  }


  private fun newBalance(accountId: String, amount: Int) {
    accounts.compute(accountId) { k, v -> requireNonNull(v)!!.plus(amount) }
    LOGGER.info("Changing Balance: {} - {}", accountId, amount)

    LOGGER.info("ACCOUNTS:")
    LOGGER.info(accounts.toString())
  }


  class BankAccountAuditEvent(
    val sequenceNumber: Long, timestamp: Instant, val accountId: String,
    val amount: Int, val traceId: String, val correlationId: String
  ) {
    private val timestamp: Instant = timestamp

    fun getTimestamp(): Instant {
      return timestamp
    }

    override fun equals(other: Any?): Boolean {
      if (other == null) return false
      if (this === other) {
        return true
      }
      if (other == null || this::class.java !== other::class.java) {
        return false
      }
      val that = other as BankAccountAuditEvent
      return (sequenceNumber == that.sequenceNumber && timestamp.equals(that.timestamp) && accountId
        .equals(that.accountId) && amount == that.amount && traceId.equals(that.traceId)
        && correlationId.equals(that.correlationId))
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


  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(CurrentBalanceProjection::class.java)
  }
}
