@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package io.holixon.axon.avro.serializer.spring._test

import bankaccount.BankAccount
import bankaccount.BankAccountApi
import bankaccount.command.CreateBankAccount
import bankaccount.projection.CurrentBalanceProjection
import bankaccount.query.BankAccountAuditQuery
import bankaccount.query.CurrentBalanceQueries
import bankaccount.query.CurrentBalanceResult
import bankaccount.query.CurrentBalanceResultList
import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration.Companion.EVENT_SERIALIZER
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration.Companion.MESSAGE_SERIALIZER
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerSpringBase
import io.holixon.axon.avro.serializer.spring.EnableAxonAvroSerializer
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.toolisticon.avro.kotlin.AvroSchemaResolver
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.bind.annotation.*

@SpringBootApplication
@ComponentScan(basePackageClasses = [BankAccount::class, BankTestApplication::class])
@ActiveProfiles(AxonAvroSerializerSpringBase.PROFILE_ITEST)
@EntityScan(
  basePackageClasses = [
    DomainEventEntry::class, SagaEntry::class, TokenEntry::class, DeadLetterEntry::class
  ]
)
@OpenAPIDefinition(
  info = Info(title = "Bank Fixture Application", version = "1"),
  externalDocs = ExternalDocumentation(description = "h2-console", url = "/h2-console")
)
class BankTestApplication {
  companion object : KLogging()

  object PROFILES {
    const val JACKSON_NO_SERVER = "jackson-no-server"
    const val JACKSON_SERVER = "jackson-server"
    const val AVRO_SERVER = "avro-server"
    const val AVRO_NO_SERVER = "avro-no-server"
  }


  abstract class ProfileConfiguration(
    serializer: String, serverEnabled: Boolean
  ) {
    init {
      logger.info { "----- Running config: serializer=$serializer, server-enabled=$serverEnabled" }
    }
  }

  @Configuration
  @Profile(PROFILES.JACKSON_NO_SERVER)
  @PropertySource("classpath:profiles/server-disabled.properties")
  @PropertySource("classpath:profiles/serializer-jackson.properties")
  class JacksonNoServerConfiguration : ProfileConfiguration("jackson", false) {
    @Bean
    @Primary
    fun objectMapper() = BankAccountApi.configureObjectMapper()
  }

  @Configuration
  @Profile(PROFILES.JACKSON_SERVER)
  @PropertySource("classpath:profiles/server-enabled.properties")
  @PropertySource("classpath:profiles/serializer-jackson.properties")
  class JacksonServerConfiguration : ProfileConfiguration("jackson", true) {
    @Bean
    @Primary
    fun objectMapper() = BankAccountApi.configureObjectMapper()
  }


  @Configuration
  @EnableAxonAvroSerializer
  @Profile(PROFILES.AVRO_NO_SERVER)
  @PropertySource("classpath:profiles/server-disabled.properties")
  class AvroNoServerConfiguration : ProfileConfiguration("avro", false) {

    @Bean
    @Primary
    fun defaultSerializer(builder: AvroSerializer.Builder): Serializer = builder.avro4k(avro4k = avro4k).build()

    @Bean
    @Qualifier("eventSerializer")
    fun eventSerializer(builder: AvroSerializer.Builder): Serializer = builder.avro4k(avro4k = avro4k).build()

    @Bean
    @Qualifier("messageSerializer")
    fun messageSerializer(builder: AvroSerializer.Builder): Serializer = builder.avro4k(avro4k = avro4k).build()

    @Bean
    fun schemaResolver() = BankAccountSchemas.schemaResolver
  }

  @Configuration
  @EnableAxonAvroSerializer
  @Profile(PROFILES.AVRO_SERVER)
  @PropertySource("classpath:profiles/server-enabled.properties")
  class AvroServerConfiguration : ProfileConfiguration("avro", true) {

    @Bean
    fun storageEngine(
      emp: EntityManagerProvider,
      txManager: TransactionManager,
      @Qualifier(EVENT_SERIALIZER)
      eventSerializer: Serializer
    ): EventStorageEngine = JpaEventStorageEngine.builder()
      .entityManagerProvider(emp)
      .eventSerializer(eventSerializer)
      .snapshotSerializer(eventSerializer)
      .transactionManager(txManager)
      .build()

    @Bean
    @Primary
    fun defaultSerializer(builder: AvroSerializer.Builder): Serializer = JacksonSerializer.builder()
      .build()

    @Bean
    @Qualifier(EVENT_SERIALIZER)
    fun eventSerializer(builder: AvroSerializer.Builder): Serializer = builder.avro4k(avro4k = avro4k).build()

    @Bean
    @Qualifier(MESSAGE_SERIALIZER)
    fun messageSerializer(builder: AvroSerializer.Builder): Serializer = builder.avro4k(avro4k = avro4k).build()

    @Bean
    fun schemaResolver(): AvroSchemaResolver = BankAccountSchemas.schemaResolver
  }


  @Bean
  fun currentBalanceQueries(queryGateway: QueryGateway): CurrentBalanceQueries =
    CurrentBalanceQueries(queryGateway)

  @Bean
  fun bankAccountAuditEventQuery(queryGateway: QueryGateway): BankAccountAuditQuery =
    BankAccountAuditQuery.create(queryGateway)

  @Bean
  fun projection() = CurrentBalanceProjection()


  @RestController
  @RequestMapping("/bank")
  class BankController(
    val commandGateway: CommandGateway,
    val currentBalanceQueries: CurrentBalanceQueries
  ) {

    @PostMapping("/accounts")
    fun createAccount(id: String, amount: Int, currency: String): String {
      return commandGateway.sendAndWait(CreateBankAccount(id, Money.of(amount, currency)))
    }

    @GetMapping("/accounts")
    fun getAll(): ResponseEntity<CurrentBalanceResultList> {
      return ResponseEntity.ok(currentBalanceQueries.findAll().join())
    }

    @GetMapping("/current-balance/{accountId}")
    fun getCurrentBalance(@PathVariable accountId: String): ResponseEntity<CurrentBalanceResult> =
      ResponseEntity.ok(currentBalanceQueries.findByAccountId(accountId).join())
  }
}

fun main() = runApplication<BankTestApplication>().let { }
