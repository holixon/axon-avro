package bank

import bankaccount.MassOperations
import mu.KLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class LoadGeneratorOnStartTrigger(
  private val massOperations: MassOperations
) {
  companion object : KLogging()

  @Scheduled(initialDelay = 5_000, fixedDelay = 1_000_000_000)
  fun createAccounts() {
    massOperations.createAccounts(count = 10, numberOfTransfers = 100)
  }

}
