package io.holixon.axon.avro.generation.meta

import java.util.*
import kotlin.collections.associateBy
import kotlin.let
import kotlin.text.lowercase
import kotlin.text.replaceFirstChar

enum class RecordMetaDataType {
  Event,
  Command,
  Query,
  QueryResult,
  ;

  val decapitalizedName = name.replaceFirstChar { c -> c.lowercase(Locale.getDefault()) }

  companion object {
    private val DECAPITALIZED_NAMES = RecordMetaDataType.entries.associateBy { it.decapitalizedName }

    operator fun get(name:String?) : RecordMetaDataType? = name?.let { DECAPITALIZED_NAMES[it] }

  }
}
