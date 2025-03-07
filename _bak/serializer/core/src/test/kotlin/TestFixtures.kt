package io.holixon.axon.avro.serializer

import bankaccount.event.BankAccountCreated
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.value.ByteArrayValue
import io.toolisticon.kotlin.avro.value.HexString
import io.toolisticon.kotlin.avro.value.SingleObjectEncodedBytes
import org.junit.jupiter.api.Test


object TestFixtures {

  object BankAccountCreatedFixture {

    val SCHEMA = AvroSchema(BankAccountCreated.getClassSchema())

    /**
     * id = 1, amount = 10 EUR
     */
    val SINGLE_OBJECT_ENCODED = SingleObjectEncodedBytes.parse(HexString.parse("[C3 01 00 4B 86 F4 58 00 7B 6C 02 31 10 31 45 2B 31 20 45 55 52]"))

  }

}

class FooTest {

  @Test
  fun name() {
    val s = HexString.of("""�=G5�41""".encodeToByteArray()).formatted
    println(s)
  }
}
