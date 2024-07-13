package bankaccount.event

import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema

object BankAccountEvents {

  fun getSchemas(): List<AvroSchema> = listOf(
    // Events
    AvroSchema(BankAccountCreated.getClassSchema()),
    AvroSchema(MoneyWithdrawn.getClassSchema()),
    AvroSchema(MoneyDeposited.getClassSchema()),
  )
}
