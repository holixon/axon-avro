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
     * id = 1, amount = 10
     */
    val SINGLE_OBJECT_ENCODED = SingleObjectEncodedBytes.of(bytes = ByteArrayValue.parse(HexString.parse("[C3 01 3D 47 35 17 03 EC 90 34 02 31 14]")))

  }

}

class FooTest {

  @Test
  fun name() {
    val s = HexString.of("""�=G5�41""".encodeToByteArray()).formatted
    println(s)
  }
}
