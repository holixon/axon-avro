package bank

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import bankaccount.event.BankAccountCreated
import bankaccount.event.MoneyDeposited
import bankaccount.event.MoneyWithdrawn
import bankaccount.query.*
import com.github.avrokotlin.avro4k.Avro
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.repository.avroSchemaResolver


val avro4k = Avro.default

enum class BankAccountSchemas(val schema: AvroSchema) {
  // Commands
  SCHEMA_CREATE_BANK_ACCOUNT(AvroSchema(avro4k.schema(CreateBankAccount.serializer()))),
  SCHEMA_WITHDRAW_MONEY(AvroSchema(avro4k.schema(WithdrawMoney.serializer()))),
  SCHEMA_DEPOSIT_MONEY(AvroSchema(avro4k.schema(DepositMoney.serializer()))),

  // Events
  SCHEMA_BANK_ACCOUNT_CREATED(AvroSchema(BankAccountCreated.getClassSchema())),
  SCHEMA_MONEY_WITHDRAWN(AvroSchema(MoneyWithdrawn.getClassSchema())),
  SCHEMA_MONEY_DEPOSITED(AvroSchema(MoneyDeposited.getClassSchema())),

  // Query
  SCHEMA_CURRENT_BALANCE_QUERY(AvroSchema(avro4k.schema(CurrentBalanceQuery.serializer()))),
  SCHEMA_FIND_ALL_QUERY(AvroSchema(avro4k.schema(FindAllQuery.serializer()))),

  // Query Result
  SCHEMA_CURRENT_BALANCE(AvroSchema(avro4k.schema(CurrentBalance.serializer()))),
  SCHEMA_CURRENT_BALANCE_RESULT(AvroSchema(avro4k.schema(CurrentBalanceResult.serializer()))),
  SCHEMA_CURRENT_BALANCE_RESULT_LIST(AvroSchema(avro4k.schema(CurrentBalanceResultList.serializer()))),
  ;

  companion object {

    val schemaResolver = avroSchemaResolver(entries.map { it.schema })

  }

}

