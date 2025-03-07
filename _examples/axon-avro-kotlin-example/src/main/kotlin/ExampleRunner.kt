package io.holixon.axon.avro.example.kotlin

import holi.bank.*
import holi.bank.BankAccountContextQueryGatewayExt.findAllMoneyTransfersForAccountId
import holi.bank.BankAccountContextQueryGatewayExt.findCurrentBalanceForAccountId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class ExampleRunner(
  val commandGateway: CommandGateway,
  val queryGateway: QueryGateway
) : ApplicationContextAware {


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
