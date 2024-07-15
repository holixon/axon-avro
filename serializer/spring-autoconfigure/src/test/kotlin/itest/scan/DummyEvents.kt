package io.holixon.axon.avro.serializer.spring.itest.scan

import bankaccount.conversions.MoneyLogicalType
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money

@Serializable
data class DummyEvent(
  val value: String
)

@Serializable
data class DummyEventWithMoney(
  @Serializable(with = MoneyLogicalType.MoneySerializer::class)
  val value: Money
)
