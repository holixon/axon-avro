package io.holixon.axon.avro.serializer

import bankaccount.event.BankAccountCreated
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.value.ByteArrayValue
import io.toolisticon.avro.kotlin.value.HexString
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.junit.jupiter.api.Test


object TestFixtures {

  object BankAccountCreatedFixture {

    val SCHEMA = AvroSchema(BankAccountCreated.getClassSchema())

    /**
     * id = 1, amount = 10
     */
    val SINGLE_OBJECT_ENCODED = SingleObjectEncodedBytes(bytes = ByteArrayValue(HexString("[C3 01 3D 47 35 17 03 EC 90 34 02 31 14]")))

  }

}

class FooTest {

  @Test
  fun name() {
    val s = HexString("""�=G5�41""".encodeToByteArray()).formatted
    println(s)
  }
}
