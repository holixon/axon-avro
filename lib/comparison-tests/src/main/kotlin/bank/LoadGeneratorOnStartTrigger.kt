package bank

import bankaccount.MassOperations
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.time.Instant

@Component
@EnableScheduling
class LoadGeneratorOnStartTrigger(
  private val massOperations: MassOperations,
  private val environment: Environment
) {
  companion object : KLogging()

  @Autowired
  private lateinit var context: ApplicationContext

  @Scheduled(initialDelay = 5_000, fixedDelay = 1_000_000_000)
  fun createAccounts() {
    val executionTime = massOperations.createAccounts(count = 10, numberOfTransfers = 100)
    val profile = environment.activeProfiles.joinToString("-")
    val time = Instant.now()
    File("./execution-result.md")
      .appendText(
      """
          ## Run on $time

          - Profile: $profile
          - Execution time: $executionTime ms

          """.trimIndent()
      )

    val terminate = environment.getProperty("terminate", "false").toBoolean()
    if (terminate) {
      SpringApplication.exit(context);
    }
  }

}
