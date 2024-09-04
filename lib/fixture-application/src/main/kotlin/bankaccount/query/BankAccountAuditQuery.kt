package bankaccount.query

import bankaccount.event.BankAccountAuditEvent
import bankaccount.query.BankAccountAuditQuery.BankAccountAuditEvents
import kotlinx.serialization.Serializable
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture
import java.util.function.Function

interface BankAccountAuditQuery : Function<String, BankAccountAuditEvents?> {
  override fun apply(accountId: String): BankAccountAuditEvents? {
    return findByAccountId(accountId).join()
  }

  fun findByAccountId(accountId: String): CompletableFuture<BankAccountAuditEvents>

  @Serializable
  data class FindBankAccountAuditEventByAccountId(val accountId: String)

  @Serializable
  data class BankAccountAuditEvents(val events: List<BankAccountAuditEvent>)

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
