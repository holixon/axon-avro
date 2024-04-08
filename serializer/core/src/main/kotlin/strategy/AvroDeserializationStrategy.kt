package io.holixon.axon.avro.serializer.strategy

import org.apache.avro.generic.GenericData

interface AvroDeserializationStrategy {

  fun canDeserialize(serializedType: Class<*>) : Boolean

  fun <T: Any> deserialize(serializedType: Class<*>, data: GenericData.Record) : T

}
