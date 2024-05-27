package io.holixon.axon.avro.serializer.strategy

import org.apache.avro.generic.GenericRecord

interface AvroSerializationStrategy {

  fun canSerialize(serializedType: Class<*>): Boolean

  fun serialize(data: Any): GenericRecord
}
