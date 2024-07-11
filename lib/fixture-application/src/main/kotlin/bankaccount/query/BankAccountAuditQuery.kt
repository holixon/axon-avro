package bankaccount.query

import bankaccount.projection.CurrentBalanceProjection.BankAccountAuditEvent
import bankaccount.query.BankAccountAuditQuery.BankAccountAuditEvents
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture
import java.util.function.Function

interface BankAccountAuditQuery : Function<String, BankAccountAuditEvents?> {
  override fun apply(accountId: String): BankAccountAuditEvents? {
    return findByAccountId(accountId).join()
  }

  fun findByAccountId(accountId: String): CompletableFuture<BankAccountAuditEvents>

  class FindBankAccountAuditEventByAccountId(val accountId: String)

  class BankAccountAuditEvents(events: List<BankAccountAuditEvent>) {
    private val events: List<BankAccountAuditEvent> = events

    fun getEvents(): List<BankAccountAuditEvent> {
      return events
    }

    @Override
    override fun toString(): String {
      return "BankAccountAuditEvents{" +
        "events=" + events +
        '}'
    }
  }

  companion object {
    fun create(queryGateway: QueryGateway): BankAccountAuditQuery = object : BankAccountAuditQuery {
      override fun findByAccountId(accountId: String): CompletableFuture<BankAccountAuditEvents> {
        return queryGateway
          .query(
            FindBankAccountAuditEventByAccountId(accountId), ResponseTypes.instanceOf(
              BankAccountAuditEvents::class.java
            )
          )
      }
    }
  }

}
