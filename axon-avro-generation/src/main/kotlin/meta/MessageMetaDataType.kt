package io.holixon.axon.avro.generation.meta

import java.util.*

/**
 * Message meta data types, inspired by FModel
 * @see https://github.com/fraktalio
 */
enum class MessageMetaDataType {
  Decider,
  Factory,
  Query;

  val decapitalizedName = name.replaceFirstChar { c -> c.lowercase(Locale.getDefault()) }

  companion object {
    // TODO: support lower camel case to upper snake case
    private val NAMES: Map<String, MessageMetaDataType> = MessageMetaDataType.entries.associateBy { it.decapitalizedName }

    operator fun get(name: String?): MessageMetaDataType? = name?.let { NAMES[it] }
  }

}
