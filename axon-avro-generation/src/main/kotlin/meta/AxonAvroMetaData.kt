package io.holixon.axon.avro.generation.meta

import io.holixon.axon.avro.generation.meta.FieldMetaData.Companion.fieldMetaData
import io.holixon.axon.avro.generation.meta.RecordMetaData.Companion.recordMetaData
import io.toolisticon.kotlin.avro.generator.api.AvroPoetType
import io.toolisticon.kotlin.avro.model.AvroType
import io.toolisticon.kotlin.avro.model.RecordField
import io.toolisticon.kotlin.avro.model.RecordType
import io.toolisticon.kotlin.avro.model.wrapper.AvroProtocol
import io.toolisticon.kotlin.avro.value.property.AvroMetaData

/**
 * Access meta data from avro schema for axon/ddd declarations.
 *
 *
 */
sealed interface AxonAvroMetaData : AvroMetaData {
  companion object {
    fun AvroType.metaData(): AxonAvroMetaData? {
      return when (this) {
        is RecordField -> fieldMetaData()
        is RecordType -> recordMetaData()
        else -> null
      }
    }


  }

}
