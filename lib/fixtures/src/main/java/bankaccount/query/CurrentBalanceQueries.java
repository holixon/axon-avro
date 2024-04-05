package bankaccount.query;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;

import java.util.concurrent.CompletableFuture;

public class CurrentBalanceQueries {
  private final QueryGateway queryGateway;

  public CurrentBalanceQueries(QueryGateway queryGateway) {
    this.queryGateway = queryGateway;
  }

  public CompletableFuture<CurrentBalanceResult> findByAccountId(String accountId) {
    return queryGateway.query(new CurrentBalanceQuery(accountId), ResponseTypes.instanceOf(CurrentBalanceResult.class));
  }

  public CompletableFuture<CurrentBalanceResultList> findAll() {
    return queryGateway.query(FindAllQuery.getINSTANCE(), ResponseTypes.instanceOf(CurrentBalanceResultList.class));
  }

}
