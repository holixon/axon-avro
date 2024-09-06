package io.holixon.axon.avro.generation.strategy

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.holixon.axon.avro.generation.TestFixtures
import io.toolisticon.kotlin.avro.generator.spi.AvroCodeGenerationSpiRegistry
import io.toolisticon.kotlin.avro.generator.spi.ProtocolDeclarationContext
import io.toolisticon.kotlin.generation.spi.strategy.KotlinCodeGenerationStrategyList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalKotlinPoetApi::class)
class AxonEventHandlerInterfaceStrategyTest {

  private val declaration = TestFixtures.parseProtocol("BankAccountProtocol.avpr")
  private val strategy = AxonEventHandlerInterfaceStrategy()

  private val registry = AvroCodeGenerationSpiRegistry(
    strategies = KotlinCodeGenerationStrategyList(strategy)
  )

  private val context = ProtocolDeclarationContext.of(declaration, registry, TestFixtures.DEFAULT_PROPERTIES)

  @Test
  fun `create event handler interfaces`() {
    val file = strategy.invoke(context, declaration)

    assertThat(file.code).isEqualToIgnoringWhitespace("""
      package holi.bank

      import jakarta.`annotation`.Generated
      import org.axonframework.eventhandling.EventHandler

      /**
       * Protocol declaration for bank-account.
       */
      @Generated(value = ["io.toolisticon.kotlin.avro.generator.AvroKotlinGenerator"], date =
          "2024-08-21T23:19:02.152209Z")
      public object BankAccountContextEventHandlers {
        public interface BankAccountCreatedEventEventHandler {
          @EventHandler
          public fun onBankAccountCreatedEvent(event: BankAccountCreatedEvent)
        }

        public interface MoneyDepositedEventEventHandler {
          @EventHandler
          public fun onMoneyDepositedEvent(event: MoneyDepositedEvent)
        }

        public interface MoneyWithdrawnEventEventHandler {
          @EventHandler
          public fun onMoneyWithdrawnEvent(event: MoneyWithdrawnEvent)
        }

        /**
         * Union interface for all event handlers.
         */
        public interface BankAccountContextAllEventHandlers : BankAccountCreatedEventEventHandler,
            MoneyDepositedEventEventHandler, MoneyWithdrawnEventEventHandler
      }
    """.trimIndent())

  }

}
