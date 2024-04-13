package io.holixon.axon.avro.serializer.spring

import bankaccount.command.CreateBankAccount
import com.github.avrokotlin.avro4k.Avro
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DataClassTest {

  @Test
  fun `delivers schema`() {
    val schema = Avro.default.schema(CreateBankAccount.serializer())
    assertThat(schema).isNotNull
  }
}
