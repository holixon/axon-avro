package bankaccount

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import java.time.Instant
import java.util.*
import kotlin.random.Random

open class MassOperations(
  private val commandGateway: CommandGateway
) {

  companion object : KLogging()

  open fun createAccounts(count: Int, numberOfTransfers: Int = 0) {
    val start = Instant.now()
    logger.info { "Creating $count accounts." }
    (1..count).forEach {
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
    val time: Long = end.toEpochMilli().minus(start.toEpochMilli())
    logger.info { "Creating $count accounts finished, it took ${time}ms." }
  }

}
