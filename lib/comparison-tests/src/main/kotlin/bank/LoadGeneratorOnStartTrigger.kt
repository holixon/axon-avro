package bank

import bankaccount.MassOperations
import mu.KLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class LoadGeneratorOnStartTrigger(
  private val massOperations: MassOperations
) {
  companion object : KLogging()

  @EventListener(ApplicationStartedEvent::class)
  fun createAccounts() {
    massOperations.createAccounts(count = 10, numberOfTransfers = 100)
  }

}
