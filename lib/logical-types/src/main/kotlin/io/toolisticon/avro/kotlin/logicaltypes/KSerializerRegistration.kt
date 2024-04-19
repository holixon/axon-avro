package io.toolisticon.avro.kotlin.logicaltypes

import kotlinx.serialization.KSerializer

/**
 * [T] target type
 */
interface KSerializerRegistration<T: Any> {
  fun targetClass(): Class<T>
  fun kSerializer(): KSerializer<T>
}
