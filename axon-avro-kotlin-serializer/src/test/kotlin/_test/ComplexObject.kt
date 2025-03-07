package io.holixon.axon.avro.serializer._test

import kotlinx.serialization.Serializable

@Serializable
data class ComplexObject(
  val value1: String,
  val value2: String? = "default value",
  val value3: Int,
)
