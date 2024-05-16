package io.holixon.axon.avro.serializer.spring._test

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import bankaccount.event.BankAccountCreated
import bankaccount.event.MoneyDeposited
import bankaccount.event.MoneyWithdrawn
import bankaccount.query.*
import io.toolisticon.avro.kotlin.avroSchemaResolver
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization


val avroSerialization = AvroKotlinSerialization()

enum class BankAccountSchemas(val schema: AvroSchema) {
  // Commands
  SCHEMA_CREATE_BANK_ACCOUNT(avroSerialization.schema(CreateBankAccount::class)),
  SCHEMA_WITHDRAW_MONEY(avroSerialization.schema(WithdrawMoney::class)),
  SCHEMA_DEPOSIT_MONEY(avroSerialization.schema(DepositMoney::class)),

  // Events
  SCHEMA_BANK_ACCOUNT_CREATED(AvroSchema(BankAccountCreated.getClassSchema())),
  SCHEMA_MONEY_WITHDRAWN(AvroSchema(MoneyWithdrawn.getClassSchema())),
  SCHEMA_MONEY_DEPOSITED(AvroSchema(MoneyDeposited.getClassSchema())),

  // Query
  SCHEMA_CURRENT_BALANCE_QUERY(avroSerialization.schema(CurrentBalanceQuery::class)),
  SCHEMA_FIND_ALL_QUERY(avroSerialization.schema(FindAllQuery::class)),

  // Query Result
  SCHEMA_CURRENT_BALANCE(avroSerialization.schema(CurrentBalance::class)),
  SCHEMA_CURRENT_BALANCE_RESULT(avroSerialization.schema(CurrentBalanceResult::class)),
  SCHEMA_CURRENT_BALANCE_RESULT_LIST(avroSerialization.schema(CurrentBalanceResultList::class)),
  ;

  companion object {

    val schemaResolver = avroSchemaResolver(entries.map { it.schema })

  }

}

