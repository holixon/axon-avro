package bank

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

@Component
class LoadGenerator(
  private val commandGateway: CommandGateway
) {
  companion object: KLogging()

  @EventListener(ApplicationStartedEvent::class)
  fun createAccounts() {
    createAccounts(count = 10, numberOfTransfers = 100)
  }

  fun createAccounts(count: Int, numberOfTransfers: Int = 0) {
    val start = Instant.now()
    logger.info { "Creating $count accounts." }
    (0 .. count).forEach {
      val accountId = UUID.randomUUID().toString()
      commandGateway.sendAndWait<Void>(
        CreateBankAccount(accountId = accountId, initialBalance = 100)
      )
      logger.info { "Created account $it: $accountId" }
      if (numberOfTransfers > 0) {
        repeat(numberOfTransfers) {
          commandGateway.sendAndWait<Void>(
            DepositMoney(accountId = accountId, amount = Random.nextInt(10, 20))
          )
          commandGateway.sendAndWait<Void>(
            WithdrawMoney(accountId = accountId, amount = Random.nextInt(1, 11))
          )
        }
      }
    }
    val end = Instant.now()
    val time = end.minusMillis(start.toEpochMilli())
    logger.info { "Creating $count accounts finished, it took ${time}ms." }
  }
}
