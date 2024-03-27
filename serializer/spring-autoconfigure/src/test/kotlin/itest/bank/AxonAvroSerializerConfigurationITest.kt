@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package io.holixon.axon.avro.serializer.spring.itest.bank

import bankaccount.BankAccount
import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.event.BankAccountCreated
import bankaccount.event.MoneyDeposited
import bankaccount.event.MoneyWithdrawn
import bankaccount.projection.CurrentBalanceProjection
import bankaccount.query.BankAccountAuditQuery
import bankaccount.query.CurrentBalanceQueries
import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerSpringBase.PROFILE_ITEST
import io.toolisticon.avro.kotlin.AvroSchemaResolver
import io.toolisticon.avro.kotlin.avroSchemaResolver
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.CompactDriver
import org.axonframework.serialization.xml.XStreamSerializer
import org.axonframework.test.server.AxonServerContainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*


@SpringBootTest(classes = [AxonAvroSerializerConfigurationITest.AxonAvroSerializerConfigurationITestApplication::class], webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles(PROFILE_ITEST)
@ContextConfiguration(initializers = [ AxonAvroSerializerConfigurationITest.Initializer::class ])
internal class AxonAvroSerializerConfigurationITest {
  companion object : KLogging() {
    @Container
    val axon = AxonServerContainer()
  }

  internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
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
  @Qualifier("eventSerializer")
  lateinit var eventSerializer: Serializer

  @BeforeEach
  internal fun ensure_serializer() {
    assertThat(eventSerializer).isInstanceOf(AvroSerializer::class.java)
  }

  @Test
  internal fun `create account and deposit money`() {
    val accountId = UUID.randomUUID().toString()

    assertThat(queries.findByAccountId(accountId).join()).isEmpty

    commandGateway.sendAndWait<Any>(CreateBankAccount(accountId, 100))

    await.untilAsserted {
      assertThat(queries.findByAccountId(accountId).join()).isNotEmpty
    }

    val auditEvents = auditEventQuery.apply(accountId)
    assertThat(auditEvents.events).isNotEmpty
    assertThat(auditEvents.events.first().correlationId).isNotNull

    logger.info { "auditEvents for accountId='$accountId': ${auditEventQuery.apply(accountId)}" }

    commandGateway.sendAndWait<Any>(DepositMoney(accountId, 50))

    await.untilAsserted {
      assertThat(queries.findByAccountId(accountId).join().orElseThrow().balance).isEqualTo(150)
    }

    logger.info { "auditEvents for accountId='$accountId': ${auditEventQuery.apply(accountId)}" }
  }


  @SpringBootApplication
  @Import(AxonAvroSerializerConfiguration::class)
  @ComponentScan(basePackageClasses = [BankAccount::class])
  @Profile(PROFILE_ITEST)
  class AxonAvroSerializerConfigurationITestApplication {

    private val xstreamSerializer = XStreamSerializer
      .builder()
      .xStream(XStream(CompactDriver())
        .apply { addPermission(AnyTypePermission()) })
      .disableAxonTypeSecurity()
      .build()

    @Bean
    fun projection() = CurrentBalanceProjection()

    @Bean
    fun schemaResolver() = avroSchemaResolver(
      listOf(
        BankAccountCreated.getClassSchema(),
        MoneyDeposited.getClassSchema(),
        MoneyWithdrawn.getClassSchema()
      ).map { AvroSchema(it) }
    )

    @Bean
    fun currentBalanceQueries(queryGateway: QueryGateway): CurrentBalanceQueries = CurrentBalanceQueries(queryGateway)

    @Bean
    fun bankAccountAuditEventQuery(queryGateway: QueryGateway): BankAccountAuditQuery = BankAccountAuditQuery.create(queryGateway)


    @Bean
    @Primary
    @Qualifier("defaultSerializer")
    fun defaultSerializer(schemaResolver: AvroSchemaResolver): Serializer = xstreamSerializer

    @Bean
    @Qualifier("messageSerializer")
    fun messageSerializer() : Serializer = xstreamSerializer

    @Bean
    @Qualifier("eventSerializer")
    fun eventSerializer(schemaResolver: AvroSchemaResolver): Serializer = AvroSerializer.builder().avroSchemaResolver(schemaResolver).build()
  }
}
