package io.holixon.axon.avro.generation.meta

import io.holixon.axon.avro.generation.meta.RecordMetaData.Companion.KEYS.REVISION
import io.holixon.axon.avro.generation.meta.RecordMetaData.Companion.KEYS.TYPE
import io.toolisticon.kotlin.avro.model.RecordType
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.value.CanonicalName
import io.toolisticon.kotlin.avro.value.Name
import io.toolisticon.kotlin.avro.value.Namespace
import io.toolisticon.kotlin.avro.value.property.meta
import io.toolisticon.kotlin.avro.value.property.metaData

data class RecordMetaData(
  val namespace: Namespace,
  val name: Name,
  val revision: String? = null,
  val type: RecordMetaDataType? = null
) : AxonAvroMetaData {
  companion object {
    object KEYS {
      const val REVISION = "revision"
      const val TYPE = "type"
    }

    fun RecordType.recordMetaData(): RecordMetaData? = this.schema.recordMetaData()
    fun AvroSchema.recordMetaData(): RecordMetaData? = this.metaData {
      val meta = this.properties.meta


      RecordMetaData(
        namespace = this.namespace,
        name = this.name,
        revision = meta.getValueOrNull(REVISION),
        type = meta.getValueOrNull<String>(TYPE)?.let { RecordMetaDataType[it.trim()] }
      )
    }

  }

  val fullName: CanonicalName = namespace + name
}
