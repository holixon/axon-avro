package io.holixon.axon.avro.serializer.spring

import bankaccount.command.CreateBankAccount
import com.github.avrokotlin.avro4k.Avro
import org.junit.jupiter.api.Test

class DataClassTest {

  @Test
  fun name() {
    println(Avro.default.schema(CreateBankAccount.serializer()))
  }
}
