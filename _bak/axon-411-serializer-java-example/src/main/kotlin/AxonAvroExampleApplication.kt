package holi.bank

import bankaccount.BankAccount
import bankaccount.BankAccountApi
import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import bankaccount.projection.CurrentBalanceProjection
import bankaccount.query.CurrentBalanceQueries
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.spring.serialization.avro.AvroSchemaScan
import org.javamoney.moneta.Money
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.*

fun main() {
  System.setProperty("disable-axoniq-console-message", "true")
  runApplication<AxonAvroExampleApplication>()
}

@SpringBootApplication
class AxonAvroExampleApplication {

  @Configuration
  @AvroSchemaScan(
    basePackages = ["bankaccount"],// commands, events, queries
  )
  @ComponentScan(basePackageClasses = [BankAccount::class])
  @EntityScan(
    basePackageClasses = [
      DomainEventEntry::class, SagaEntry::class, TokenEntry::class, DeadLetterEntry::class
    ]
  )
  class AvroSerializerConfiguration {
    @Bean
    @Primary
    fun objectMapper() = BankAccountApi.configureObjectMapper()

    @Bean
    fun currentBalanceQueries(queryGateway: QueryGateway) = CurrentBalanceQueries(queryGateway)

    @Bean
    fun projection() = CurrentBalanceProjection()

  }


  @Component
  class ExampleRunner(
    val commandGateway: CommandGateway,
    val currentBalanceQueries: CurrentBalanceQueries
  ) : ApplicationContextAware {

    companion object : KLogging()

    private lateinit var applicationContext: ApplicationContext

    @EventListener
    fun runExample(event: ApplicationStartedEvent) {
      logger.info {
        """
          ===============================================================================



                                         S H O W T I M E



          ================================================================================
        """.trimIndent()
      }

      val bankAccountId = UUID.randomUUID().toString()
      val createdAccountId = commandGateway.sendAndWait<Any>(
        CreateBankAccount(accountId = bankAccountId, initialBalance = Money.of(100, "EUR"))
      )

      logger.info {
        """
        ================================================================================

           Created bank account id: $createdAccountId

        ================================================================================
      """.trimIndent()
      }

      logger.info { "Doing some money transfer: $bankAccountId" }
      commandGateway.send<Void>(DepositMoney(accountId = bankAccountId, amount = Money.of(99, "EUR"))).join()
      commandGateway.send<Void>(WithdrawMoney(accountId = bankAccountId, amount = Money.of(77, "EUR"))).join()

      logger.info { "Taking a nap." }
      // wait two secs
      Thread.sleep(2000)

      val currentBalance = currentBalanceQueries.findByAccountId(accountId = bankAccountId).join()

      logger.info {
        """
         ================================================================================

           query: `queryGateway.findCurrentBalanceForAccountId(FindCurrentBalanceByAccountIdQuery(accountId = bankAccountId))`

           Current balance for account $bankAccountId: $currentBalance

         ================================================================================
      """.trimIndent()
      }

      Thread.sleep(2000)

      logger.info {
        """
         ================================================================================

           query: `queryGateway.findAllMoneyTransfersForAccountId(FindAllMoneyTransfersByAccountIdQuery(accountId = bankAccountId))`

           Transactions for account $bankAccountId:

         ================================================================================
      """.trimIndent()
      }

      Thread.sleep(2000)
      logger.info {
        """
          ===============================================================================
                                             D O N E !
          ===============================================================================
        """.trimIndent()
      }

      SpringApplication.exit(applicationContext, { 0 })
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
      this.applicationContext = applicationContext
    }

  }
}
