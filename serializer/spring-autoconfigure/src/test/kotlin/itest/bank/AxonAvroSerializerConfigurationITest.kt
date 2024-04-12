@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package io.holixon.axon.avro.serializer.spring.itest.bank

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.query.BankAccountAuditQuery
import bankaccount.query.CurrentBalanceQueries
import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration.Companion.EVENT_SERIALIZER
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration.Companion.MESSAGE_SERIALIZER
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerSpringBase.PROFILE_ITEST
import io.holixon.axon.avro.serializer.spring._test.BankTestApplication
import io.holixon.axon.avro.serializer.spring._test.BankTestApplication.PROFILES
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.serialization.Serializer
import org.axonframework.test.server.AxonServerContainer
import org.javamoney.moneta.Money
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*



@SpringBootTest(classes = [BankTestApplication::class], webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles(PROFILE_ITEST, PROFILES.AVRO_SERVER)
@ContextConfiguration(initializers = [AxonAvroSerializerConfigurationITest.Initializer::class])
internal class AxonAvroSerializerConfigurationITest {
  companion object : KLogging() {
    @Container
    val axon: AxonServerContainer = AxonServerContainer(
      DockerImageName.parse("axoniq/axonserver:2024.0.0-jdk-17")
        .asCompatibleSubstituteFor("axoniq/axonserver:latest-dev")
    )
      .withEnv(
        mapOf(
          "SPRING_MAIN_BANNER-MODE" to "OFF"
        )
      )
      .withLogConsumer(
        Slf4jLogConsumer(LoggerFactory.getLogger("tc.axonserver"))
          .withSeparateOutputStreams()
          .withPrefix("tc.axonserver")
      )

  }

  internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
      System.setProperty("disable-axoniq-console-message", "true")
      TestPropertyValues.of(
        "axon.axonserver.servers=localhost:${axon.grpcPort}",
      ).applyTo(configurableApplicationContext.environment)
    }
  }


  @Autowired
  lateinit var commandGateway: CommandGateway

  @Autowired
  lateinit var queries: CurrentBalanceQueries

  @Autowired
  lateinit var auditEventQuery: BankAccountAuditQuery

  @Autowired
  @Qualifier(EVENT_SERIALIZER)
  lateinit var eventSerializer: Serializer

  @Autowired
  @Qualifier(MESSAGE_SERIALIZER)
  lateinit var messageSerializer: Serializer

  @BeforeEach
  internal fun ensure_serializer() {
    assertThat(eventSerializer).isInstanceOf(AvroSerializer::class.java)
    assertThat(messageSerializer).isInstanceOf(AvroSerializer::class.java)
  }

  @Test
  fun `create account and deposit money`() {
    val accountId = UUID.randomUUID().toString()

    assertThat(queries.findByAccountId(accountId).join().value).isNull()
    assertThat(queries.findAll().join()).isEmpty()

    commandGateway.sendAndWait<Any>(CreateBankAccount(accountId, Money.of(100, "EUR")))

    await.untilAsserted {
      val currentBalance = queries.findByAccountId(accountId).join()
      assertThat(currentBalance.value).isNotNull
      logger.info { "current: ${currentBalance.value}" }
    }

//    val auditEvents = auditEventQuery.apply(accountId)
//    assertThat(auditEvents.events).isNotEmpty
//    assertThat(auditEvents.events.first().correlationId).isNotNull

    logger.info { "auditEvents for accountId='$accountId': ${auditEventQuery.apply(accountId)}" }

    commandGateway.sendAndWait<Any>(DepositMoney(accountId, Money.of(50, "EUR")))

    await.untilAsserted {
      assertThat(queries.findByAccountId(accountId).join().value?.balance).isEqualTo(150)
    }

    //logger.info { "auditEvents for accountId='$accountId': ${auditEventQuery.apply(accountId)}" }
  }

}
