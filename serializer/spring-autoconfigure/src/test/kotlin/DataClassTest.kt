package io.holixon.axon.avro.serializer.spring

import bankaccount.command.CreateBankAccount
import bankaccount.conversions.MoneySerializer
import com.github.avrokotlin.avro4k.Avro
import com.github.avrokotlin.avro4k.serializer.UUIDSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DataClassTest {

  @Test
  fun `delivers schema`() {
    val schema = Avro.default.schema(CreateBankAccount.serializer())
    assertThat(schema).isNotNull
  }
}
