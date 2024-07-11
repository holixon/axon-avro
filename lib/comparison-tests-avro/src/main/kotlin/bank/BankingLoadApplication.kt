package bank

import bankaccount.BankAccount
import bankaccount.BankAccountApi
import bankaccount.MassOperations
import bankaccount.projection.CurrentBalanceProjection
import bankaccount.query.BankAccountAuditQuery
import bankaccount.query.CurrentBalanceQueries
import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.EnableAxonAvroSerializer
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary

fun main() {
  System.setProperty("disable-axoniq-console-message", "true")
  runApplication<BankingLoadApplication>()
}

@SpringBootApplication
@ComponentScan(basePackageClasses = [BankAccount::class, BankingLoadApplication::class])
@EntityScan(
  basePackageClasses = [
    DomainEventEntry::class, SagaEntry::class, TokenEntry::class, DeadLetterEntry::class
  ]
)
@EnableAxonAvroSerializer
class BankingLoadApplication {

  @Bean
  fun currentBalanceQueries(queryGateway: QueryGateway): CurrentBalanceQueries = CurrentBalanceQueries(queryGateway)

  @Bean
  fun bankAccountAuditEventQuery(queryGateway: QueryGateway): BankAccountAuditQuery = BankAccountAuditQuery.create(queryGateway)

  @Bean
  fun projection() = CurrentBalanceProjection()

  @Bean
  fun massOperations(commandGateway: CommandGateway) = MassOperations(commandGateway)

  @Bean
  @Primary
  fun defaultSerializer(): Serializer = JacksonSerializer.builder().build()

  @Bean
  @Qualifier("eventSerializer")
  fun eventSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  @Qualifier("messageSerializer")
  fun messageSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  fun schemaResolver() = BankAccountSchemas.schemaResolver
}
