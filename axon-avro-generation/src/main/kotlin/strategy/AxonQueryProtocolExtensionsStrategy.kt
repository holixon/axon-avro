package io.holixon.axon.avro.generation.strategy

import com.squareup.kotlinpoet.*
import io.toolisticon.kotlin.avro.declaration.ProtocolDeclaration
import io.toolisticon.kotlin.avro.generator.AvroKotlinGenerator
import io.toolisticon.kotlin.avro.generator.addKDoc
import io.toolisticon.kotlin.avro.generator.asClassName
import io.toolisticon.kotlin.avro.generator.spi.ProtocolDeclarationContext
import io.toolisticon.kotlin.avro.generator.strategy.AvroFileSpecFromProtocolDeclarationStrategy
import io.toolisticon.kotlin.avro.value.Name
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder.objectBuilder
import io.toolisticon.kotlin.generation.spec.KotlinFileSpec
import io.toolisticon.kotlin.generation.support.GeneratedAnnotation
import mu.KLogging
import org.axonframework.queryhandling.QueryGateway

@OptIn(ExperimentalKotlinPoetApi::class)
class AxonQueryProtocolExtensionsStrategy : AvroFileSpecFromProtocolDeclarationStrategy() {

  companion object : KLogging()

  override fun invoke(context: ProtocolDeclarationContext, input: ProtocolDeclaration): KotlinFileSpec {

    val fileName: ClassName = (input.canonicalName.namespace + Name(input.name.value + "QueryGatewayExt")).asClassName()
    val fileBuilder = builder.fileBuilder(fileName)

    val objectBuilder = objectBuilder(fileName).apply {
      addAnnotation(GeneratedAnnotation(value = AvroKotlinGenerator.NAME).date(context.properties.nowSupplier()))
      addKDoc(input.documentation)
    }

    /*
    Single interface for each query
     */
    input.protocol.messages.filterTwoWay()
      .filter { (_, message) -> message.isQuery() }
      .forEach {  (_, message) ->
        if (message.request.fields.size == 1) {

          val queryTypeName = context.avroPoetTypes[message.request.fields.first().schema.hashCode].typeName
          val queryParameter = ParameterSpec.builder("query", queryTypeName).build()
          val responseTypeClass = requireNotNull(message.response.memberName()) { "Query must have a non-nullable response" }
          val completableFutureResultTypeName = requireNotNull(context.avroPoetTypes.resolveExtensionFunctionQueryResponseTypeName(message.response)) { "Query must have a non-nullable response" }
          val responseTypeName = context.avroPoetTypes[message.response.get().hashCode].typeName

          objectBuilder.addFunction(
            builder.funBuilder(message.name.value)
              .receiver(QueryGateway::class)
              .addParameter(queryParameter)
              .returns(completableFutureResultTypeName)
              .addCode(
                "return this.query(%L, %M(%T::class.java))",
                queryParameter.name,
                responseTypeClass,
                responseTypeName
              )
          )
        } else {
          logger.warn { "Skipped query definition $name, because it had more then one parameter, but at most one is supported." }
        }
      }

    fileBuilder.addType(objectBuilder)


    // TODO run processors

    return fileBuilder.build()
  }


  override fun test(context: ProtocolDeclarationContext, input: Any): Boolean {
    return super.test(context, input) && input is ProtocolDeclaration && input.protocol.messages.values.any { message -> message.isQuery() }
  }
}
