package holi.bank


import holi.bank.BankAccountContextQueryGatewayExt.findAllMoneyTransfersForAccountId
import holi.bank.BankAccountContextQueryGatewayExt.findCurrentBalanceForAccountId
import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.AvroSchemaScan
import io.holixon.axon.avro.serializer.spring.EnableAxonAvroSerializer
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
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
  @EnableAxonAvroSerializer
  @AvroSchemaScan(
    basePackages = ["holi.bank"],// commands, events, queries
  )
  class AvroSerializerConfiguration {

    @Bean
    @Primary
    fun defaultSerializer(): Serializer = JacksonSerializer.builder().build()

    @Bean
    @Qualifier("eventSerializer")
    fun eventSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

    @Bean
    @Qualifier("messageSerializer")
    fun messageSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()
  }


  @Component
  class ExampleRunner(
    val commandGateway: CommandGateway,
    val queryGateway: QueryGateway
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
        CreateBankAccountCommand(accountId = bankAccountId, initialBalance = 100)
      )

      logger.info {
        """
        ================================================================================

           Created bank account id: $createdAccountId

        ================================================================================
      """.trimIndent()
      }

      logger.info { "Doing some money transfer: $bankAccountId" }
      commandGateway.send<Void>(DepositMoneyCommand(accountId = bankAccountId, amount = 99)).join()
      commandGateway.send<Void>(WithdrawMoneyCommand(accountId = bankAccountId, amount = 77)).join()

      logger.info { "Taking a nap." }
      // wait two secs
      Thread.sleep(2000)

      val currentBalance = queryGateway.findCurrentBalanceForAccountId(FindCurrentBalanceByAccountIdQuery(accountId = bankAccountId)).join()

      logger.info {
        """
         ================================================================================

           query: `queryGateway.findCurrentBalanceForAccountId(FindCurrentBalanceByAccountIdQuery(accountId = bankAccountId))`

           Current balance for account $bankAccountId: $currentBalance

         ================================================================================
      """.trimIndent()
      }

      Thread.sleep(2000)
      val transactions =
        queryGateway.findAllMoneyTransfersForAccountId(FindAllMoneyTransfersByAccountIdQuery(accountId = bankAccountId)).join()

      logger.info {
        """
         ================================================================================

           query: `queryGateway.findAllMoneyTransfersForAccountId(FindAllMoneyTransfersByAccountIdQuery(accountId = bankAccountId))`

           Transactions for account $bankAccountId:

${transactions.items.joinToString(separator = "\n")}

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
