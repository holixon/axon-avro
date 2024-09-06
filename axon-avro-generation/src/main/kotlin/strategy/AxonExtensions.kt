package io.holixon.axon.avro.generation.strategy

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.holixon.axon.avro.generation.meta.MessageMetaData.Companion.messageMetaData
import io.holixon.axon.avro.generation.meta.MessageMetaDataType
import io.toolisticon.kotlin.avro.generator.api.AvroPoetTypes
import io.toolisticon.kotlin.avro.model.MessageResponse
import io.toolisticon.kotlin.avro.model.wrapper.AvroProtocol
import org.axonframework.messaging.responsetypes.ResponseTypes
import java.util.*
import java.util.concurrent.CompletableFuture

fun AvroProtocol.Message.isQuery(): Boolean {
  /*
    return this.request.fields
      .map { AvroType.avroType<AvroType>(it.schema) }
      .filterIsInstance<RecordType>()
      .mapNotNull { it.recordMetaData() }
      .any { RecordMetaDataType.Query == it.type }
    // TODO: analyze response of query
   */
  return this.messageMetaData()?.type == MessageMetaDataType.Query
}

fun AvroProtocol.Message.isDecider(): Boolean {
  return this.messageMetaData()?.type == MessageMetaDataType.Decider
}

fun AvroProtocol.Message.isDeciderInit(): Boolean {
  return this.messageMetaData()?.type == MessageMetaDataType.DeciderInit
}


/**
 * Resolves query response type.
 * TODO -> consider other collection types
 * @see https://docs.axoniq.io/reference-guide/axon-framework/queries/query-handlers
 */
fun AvroPoetTypes.resolveQueryResponseTypeName(messageResponse: MessageResponse): TypeName? =
  when (messageResponse) {
    is MessageResponse.SINGLE, is MessageResponse.MULTIPLE -> this[messageResponse.schema.hashCode].typeName
    is MessageResponse.OPTIONAL -> Optional::class.asTypeName().parameterizedBy(this[messageResponse.get().hashCode].typeName)
  }

/**
 * Resolves query response type.
 * TODO -> consider other collection types
 * @see https://docs.axoniq.io/reference-guide/axon-framework/queries/query-handlers
 */
fun AvroPoetTypes.resolveExtensionFunctionQueryResponseTypeName(
  messageResponse: MessageResponse
): TypeName = CompletableFuture::class.asTypeName()
  .parameterizedBy(resolveQueryResponseTypeName(messageResponse)!!)


fun MessageResponse.memberName(): MemberName = when (this) {
  is MessageResponse.SINGLE -> ResponseTypes::class.asClassName().member("instanceOf")
  is MessageResponse.OPTIONAL -> ResponseTypes::class.asClassName().member("optionalInstanceOf")
  is MessageResponse.MULTIPLE -> ResponseTypes::class.asClassName().member("multipleInstancesOf")
}
