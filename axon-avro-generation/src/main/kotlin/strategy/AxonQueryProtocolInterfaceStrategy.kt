package io.holixon.axon.avro.generation.strategy

import _ktx.StringKtx.firstUppercase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.KModifier
import io.holixon.axon.avro.generation.meta.MessageMetaData.Companion.messageMetaData
import io.toolisticon.kotlin.avro.declaration.ProtocolDeclaration
import io.toolisticon.kotlin.avro.generator.AvroKotlinGenerator
import io.toolisticon.kotlin.avro.generator.addKDoc
import io.toolisticon.kotlin.avro.generator.api.AvroPoetTypes
import io.toolisticon.kotlin.avro.generator.asClassName
import io.toolisticon.kotlin.avro.generator.spi.ProtocolDeclarationContext
import io.toolisticon.kotlin.avro.generator.strategy.AvroFileSpecFromProtocolDeclarationStrategy
import io.toolisticon.kotlin.avro.model.wrapper.AvroProtocol
import io.toolisticon.kotlin.avro.value.Documentation
import io.toolisticon.kotlin.avro.value.Name
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.buildFun
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder.objectBuilder
import io.toolisticon.kotlin.generation.spec.KotlinFileSpec
import io.toolisticon.kotlin.generation.spec.KotlinFunSpec
import io.toolisticon.kotlin.generation.support.GeneratedAnnotation
import mu.KLogging
import org.axonframework.queryhandling.QueryHandler

@OptIn(ExperimentalKotlinPoetApi::class)
class AxonQueryProtocolInterfaceStrategy : AvroFileSpecFromProtocolDeclarationStrategy() {

  companion object : KLogging() {
    private const val UNKNOWN_GROUP = "__UNKNOWN__"
  }

  override fun invoke(context: ProtocolDeclarationContext, input: ProtocolDeclaration): KotlinFileSpec {

    val fileName: ClassName = (input.canonicalName.namespace + Name(input.name.value + "Queries")).asClassName()
    val fileBuilder = builder.fileBuilder(fileName)

    val objectBuilder = objectBuilder(fileName).apply {
      addAnnotation(GeneratedAnnotation(value = AvroKotlinGenerator.NAME).date(context.properties.nowSupplier()))
      addKDoc(input.documentation)
    }

    val allQueriesInterfaceName = (input.canonicalName.namespace + Name(input.name.value + "AllQueries")).asClassName()
    val allQueriesInterfaceBuilder = builder.interfaceBuilder(allQueriesInterfaceName).apply {
      addKDoc(Documentation("Union interface for all queries"))
    }

    /*
    Single interface for each query
     */
    input.protocol.messages.filterTwoWay()
      .filterValues { message -> message.isQuery() }
      .entries
      .groupBy { message -> message.value.messageMetaData()?.group?.value ?: UNKNOWN_GROUP }
      .mapNotNull { (groupName, messages) ->

        // named
        if (groupName != UNKNOWN_GROUP) {
          // create one enclosing type for all queries
          listOf(
            builder
            .interfaceBuilder((input.canonicalName.namespace + Name(groupName.firstUppercase() + "Queries")).asClassName())
            .apply {
              messages
                .mapNotNull { (name, message) ->
                  if (message.request.fields.size == 1) {
                    // TODO: the strategy should be a fall-through in order: on message, on message type, on referenced-type
                    addFunction(buildQueryFunctionSpec(name, message, context.avroPoetTypes))
                  } else {
                    logger.warn { "Skipped query definition $name, because it had more then one parameter, but at most one is supported." }
                    null
                  }
                }
            }
          )
        } else {
          // anonymous group
          messages.mapNotNull { (name, message) ->
            if (message.request.fields.size == 1) {
              // create an enclosing type for each query
              builder
                .interfaceBuilder((input.canonicalName.namespace + Name(name.value.firstUppercase())).asClassName())
                .apply {
                  // TODO: the strategy should be a fall-through in order: on message, on message type, on referenced-type
                  addKDoc(message.documentation)
                  addFunction(buildQueryFunctionSpec(name, message, context.avroPoetTypes))
                }
            } else {
              logger.warn { "Skipped query definition $name, because it had more then one parameter, but at most one is supported." }
              null
            }
          }
        }
      }.flatten()
      .forEach { interfaceBuilder ->
        objectBuilder.addType(interfaceBuilder) // add interface to object
        allQueriesInterfaceBuilder.addSuperinterface(ClassName.bestGuess(interfaceBuilder.spec().className.simpleName))
      }


    objectBuilder.addType(allQueriesInterfaceBuilder) // add union interface
    fileBuilder.addType(objectBuilder)


    // TODO run processors

    return fileBuilder.build()
  }

  private fun buildQueryFunctionSpec(name: Name, message: AvroProtocol.TwoWayMessage, avroPoetTypes: AvroPoetTypes): KotlinFunSpec {
    return buildFun(name.value) {
      addModifiers(KModifier.ABSTRACT)
      addAnnotation(QueryHandler::class)
      message.request.fields.forEach { f ->
        this.addParameter(f.name.value, avroPoetTypes[f.schema.hashCode].typeName)
      }
      avroPoetTypes.resolveQueryResponseTypeName(message.response)?.let {
        returns(it)
      }
    }
  }

  override fun test(context: ProtocolDeclarationContext, input: Any): Boolean {
    return super.test(context, input) && input is ProtocolDeclaration && input.protocol.messages.values.any { message -> message.isQuery() }
  }

}
