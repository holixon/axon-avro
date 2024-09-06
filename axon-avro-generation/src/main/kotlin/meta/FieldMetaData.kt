package io.holixon.axon.avro.generation.meta

import io.holixon.axon.avro.generation.meta.FieldMetaData.Companion.KEYS.TYPE
import io.toolisticon.kotlin.avro.model.RecordField
import io.toolisticon.kotlin.avro.value.Name
import io.toolisticon.kotlin.avro.value.property.meta

data class FieldMetaData(
  val name: Name,
  val type: FieldMetaDataType?
) : AxonAvroMetaData{
  companion object {
    object KEYS {
      const val NAME = "name"
      const val TYPE = "type"
    }

    fun RecordField.fieldMetaData(): FieldMetaData? = this.properties.meta.metaData {

      FieldMetaData(
        name = this@fieldMetaData.name,
        type = this[TYPE]?.let { it as String }?.let { FieldMetaDataType[it.trim()] }
      )
    }
  }
}
