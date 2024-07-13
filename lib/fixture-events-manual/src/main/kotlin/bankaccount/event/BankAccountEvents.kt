package bankaccount.event

import com.github.avrokotlin.avro4k.Avro
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema

object BankAccountEvents {

  private val avro4k = Avro.default

  fun getSchemas(): List<AvroSchema> = listOf(
    AvroSchema(avro4k.schema(BankAccountCreated.serializer())),
    AvroSchema(avro4k.schema(MoneyWithdrawn.serializer())),
    AvroSchema(avro4k.schema(MoneyDeposited.serializer()))
  )
}
