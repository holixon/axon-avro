package io.holixon.axon.avro.serializer.strategy

import org.apache.avro.generic.GenericRecord

interface AvroDeserializationStrategy {

  fun canDeserialize(serializedType: Class<*>) : Boolean

  fun <T: Any> deserialize(serializedType: Class<*>, data: GenericRecord) : T

}
