package io.holixon.axon.avro.serializer.strategy

import io.toolisticon.avro.kotlin.AvroKotlin
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventhandling.tokenstore.ConfigToken
import org.junit.jupiter.api.Test


internal class ConfigTokenStrategyTest {

  private val strategy = ConfigTokenStrategy(AvroKotlin.defaultLogicalTypeConversions.genericData)

  @Test
  fun `serialize and deserialize config token`() {
    val token = ConfigToken(mapOf("foo" to "bar"))

    assertThat(strategy.canSerialize(token::class.java)).isTrue()
    assertThat(strategy.canDeserialize(token::class.java)).isTrue()

    val record = strategy.serialize(token)
    val deserialized : ConfigToken= strategy.deserialize(ConfigToken::class.java, record)

    assertThat(deserialized).isEqualTo(token)

  }
}
