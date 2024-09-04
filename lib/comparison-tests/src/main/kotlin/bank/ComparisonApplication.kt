package bank

import bankaccount.BankAccount
import bankaccount.MassOperations
import bankaccount.command.CreateBankAccount
import bankaccount.projection.CurrentBalanceProjection
import bankaccount.query.BankAccountAuditQuery
import bankaccount.query.CurrentBalanceQueries
import bankaccount.query.CurrentBalanceResult
import bankaccount.query.CurrentBalanceResultList
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.axonframework.queryhandling.QueryGateway
import org.javamoney.moneta.Money
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

fun main() {
  System.setProperty("disable-axoniq-console-message", "true")
  runApplication<BankingLoadApplication>()
}

@SpringBootApplication
@ComponentScan(basePackageClasses = [BankAccount::class, BankingLoadApplication::class])
@EntityScan(
  basePackageClasses = [
    DomainEventEntry::class, SagaEntry::class, TokenEntry::class, DeadLetterEntry::class
  ]
)
class BankingLoadApplication {

  @Bean
  fun currentBalanceQueries(queryGateway: QueryGateway): CurrentBalanceQueries = CurrentBalanceQueries(queryGateway)

  @Bean
  fun bankAccountAuditEventQuery(queryGateway: QueryGateway): BankAccountAuditQuery = BankAccountAuditQuery.create(queryGateway)

  @Bean
  fun projection() = CurrentBalanceProjection()

  @Bean
  fun massOperations(commandGateway: CommandGateway) = MassOperations(commandGateway)

  @RestController
  @RequestMapping("/bank")
  class BankController(
    val commandGateway: CommandGateway,
    val currentBalanceQueries: CurrentBalanceQueries
  ) {

    @PostMapping("/accounts")
    fun createAccount(id: String, amount: Int, currency: String): String {
      return commandGateway.sendAndWait(CreateBankAccount(id, Money.of(amount, currency)))
    }

    @GetMapping("/accounts")
    fun getAll(): ResponseEntity<CurrentBalanceResultList> {
      return ResponseEntity.ok(currentBalanceQueries.findAll().join())
    }

    @GetMapping("/current-balance/{accountId}")
    fun getCurrentBalance(@PathVariable accountId: String): ResponseEntity<CurrentBalanceResult> =
      ResponseEntity.ok(currentBalanceQueries.findByAccountId(accountId).join())
  }
}


