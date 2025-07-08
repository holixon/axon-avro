package io.holixon.axon.avro.generation.strategy

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.avro.generation.TestFixtures
import io.holixon.axon.avro.generation.strategy.AxonCommandHandlerProtocolInterfaceStrategy.Companion.associationField
import io.toolisticon.kotlin.avro.model.RecordType
import io.toolisticon.kotlin.avro.model.RequestType
import io.toolisticon.kotlin.avro.value.Name.Companion.toName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalKotlinPoetApi::class)
class AxonCommandHandlerProtocolInterfaceStrategyTest {
  companion object {
    val logger = KotlinLogging.logger {}
  }

  private val declaration = TestFixtures.parseProtocol("BankAccountProtocol.avpr")
  private val strategy = AxonCommandHandlerProtocolInterfaceStrategy()
  private val context = TestFixtures.protocolContext(declaration)

  @Test
  fun buildsCommandHandlerFunctionFromMessage() {

    val name = "createBankAccount".toName()
    val message = declaration.protocol.messages[name]!!
    assertThat(message.isDeciderInit()).isTrue()
    val requestType = RequestType(message.request) // request is a requestType
    val commandType = requestType.fields.first() // request has only one field
      .type as RecordType

    val associationField = commandType.associationField()
    assertThat(associationField.name).isEqualTo("accountId".toName())

    val builder = strategy.buildCommandHandlerFunction(name,message,context.avroPoetTypes)
    assertThat(builder).isNotNull
    val code = builder!!.build().code
    assertThat(code).isEqualToIgnoringWhitespace("""
        @org.axonframework.commandhandling.CommandHandler
        @org.axonframework.modelling.command.CreationPolicy(value = org.axonframework.modelling.command.AggregateCreationPolicy.ALWAYS)
        public abstract fun createBankAccount(CreateBankAccountCommand: holi.bank.CreateBankAccountCommand): kotlin.String
      """.trimIndent())
  }
}
