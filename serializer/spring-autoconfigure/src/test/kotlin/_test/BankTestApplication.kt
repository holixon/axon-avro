package io.holixon.axon.avro.serializer.spring._test

import bankaccount.BankAccount
import bankaccount.command.CreateBankAccount
import bankaccount.event.BankAccountCreated
import bankaccount.event.MoneyDeposited
import bankaccount.event.MoneyWithdrawn
import com.github.avrokotlin.avro4k.Avro
import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerSpringBase
import io.holixon.axon.avro.serializer.spring.EnableAxonAvroSerializer
import io.toolisticon.avro.kotlin.avroSchemaResolver
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.axonframework.serialization.Serializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableAxonAvroSerializer
@ComponentScan(basePackageClasses = [BankAccount::class, BankTestApplication::class])
@ActiveProfiles(AxonAvroSerializerSpringBase.PROFILE_ITEST)
@EntityScan(
  basePackageClasses = [
    DomainEventEntry::class, SagaEntry::class, TokenEntry::class, DeadLetterEntry::class
  ]
)
class BankTestApplication {

  @Bean
  fun schemaResolver() = avroSchemaResolver(listOf(
    BankAccountCreated.getClassSchema(),
    MoneyDeposited.getClassSchema(),
    MoneyWithdrawn.getClassSchema(),
    Avro.default.schema(CreateBankAccount.serializer())
  ).map { AvroSchema(it) })


  @Bean
  @Primary
  fun defaultSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  @Qualifier("eventSerializer")
  fun eventSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  @Qualifier("messageSerializer")
  fun messageSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()


  @Bean
  fun storageEngine(
    emp: EntityManagerProvider,
    txManager: TransactionManager,
    @Qualifier("eventSerializer")
    eventSerializer: Serializer
  ): EventStorageEngine = JpaEventStorageEngine.builder()
    .entityManagerProvider(emp)
    .eventSerializer(eventSerializer)
    .snapshotSerializer(eventSerializer)
    .transactionManager(txManager)
    .build()

  @RestController
  @RequestMapping("/bank")
  class BankController(
    val commandGateway: CommandGateway
  ) {

    @PostMapping("/accounts")
    fun createAccount(id: String, amount: Int): String {
      return commandGateway.sendAndWait<String>(CreateBankAccount(id, amount))
    }


  }
}

fun main() = runApplication<BankTestApplication>().let { }
