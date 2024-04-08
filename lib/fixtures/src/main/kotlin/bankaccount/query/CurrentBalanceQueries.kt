package bankaccount.query

import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture

class CurrentBalanceQueries(queryGateway: QueryGateway) {
  private val queryGateway: QueryGateway = queryGateway

  fun findByAccountId(accountId: String): CompletableFuture<CurrentBalanceResult> {
    return queryGateway.query(
      CurrentBalanceQuery(accountId),
      ResponseTypes.instanceOf(CurrentBalanceResult::class.java)
    )
  }

  fun findAll(): CompletableFuture<CurrentBalanceResultList> {
    return queryGateway.query(
      FindAllQuery.INSTANCE,
      ResponseTypes.instanceOf(CurrentBalanceResultList::class.java)
    )
  }
}
