package bankaccount

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.javamoney.moneta.Money
import java.time.Instant
import java.util.*
import kotlin.random.Random

open class MassOperations(
  private val commandGateway: CommandGateway
) {

  companion object : KLogging()

  /**
   * Create accounts, execute number of transfers and measures time.
   * @param count number of accounts to create.
   * @param numberOfTransfers number of deposit/withdraw operations to run on each account.
   * @return execution time in milliseconds.
   */
  open fun createAccounts(count: Int, numberOfTransfers: Int = 0): Long {
    val start = Instant.now()
    logger.info { "Creating $count accounts." }
    (1..count).forEach {
      val accountId = UUID.randomUUID().toString()
      commandGateway.sendAndWait<Void>(
        CreateBankAccount(accountId = accountId, initialBalance = Money.of(100, "EUR"))
      )
      logger.info { "Created account $it: $accountId" }
      if (numberOfTransfers > 0) {
        repeat(numberOfTransfers) {
          commandGateway.sendAndWait<Void>(
            DepositMoney(accountId = accountId, amount = Money.of(Random.nextInt(10, 20), "EUR"))
          )
          commandGateway.sendAndWait<Void>(
            WithdrawMoney(accountId = accountId, amount = Money.of(Random.nextInt(1, 11), "EUR"))
          )
        }
      }
    }
    val end = Instant.now()
    val time: Long = end.toEpochMilli().minus(start.toEpochMilli())
    logger.info { "Creating $count accounts finished, it took ${time}ms." }
    return time
  }

}
