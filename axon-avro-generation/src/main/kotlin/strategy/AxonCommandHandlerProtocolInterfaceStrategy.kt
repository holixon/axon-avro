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
import io.toolisticon.kotlin.avro.generator.processor.KotlinFunSpecFromProtocolMessageProcessor
import io.toolisticon.kotlin.avro.generator.spi.ProtocolDeclarationContext
import io.toolisticon.kotlin.avro.generator.strategy.AvroFileSpecFromProtocolDeclarationStrategy
import io.toolisticon.kotlin.avro.model.wrapper.AvroProtocol
import io.toolisticon.kotlin.avro.value.Documentation
import io.toolisticon.kotlin.avro.value.Name
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder.funBuilder
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder.objectBuilder
import io.toolisticon.kotlin.generation.builder.KotlinFunSpecBuilder
import io.toolisticon.kotlin.generation.spec.KotlinFileSpec
import io.toolisticon.kotlin.generation.spi.processor.executeAll
import io.toolisticon.kotlin.generation.support.GeneratedAnnotation
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler

@OptIn(ExperimentalKotlinPoetApi::class)
class AxonCommandHandlerProtocolInterfaceStrategy : AvroFileSpecFromProtocolDeclarationStrategy() {

  companion object : KLogging() {
    private const val UNKNOWN_GROUP = "__UNKNOWN__"
  }

  override fun invoke(context: ProtocolDeclarationContext, input: ProtocolDeclaration): KotlinFileSpec {

    val fileName: ClassName = (input.canonicalName.namespace + Name(input.name.value + "CommandHandlers")).asClassName()
    val fileBuilder = builder.fileBuilder(fileName)

    val objectBuilder = objectBuilder(fileName).apply {
      addAnnotation(GeneratedAnnotation(value = AvroKotlinGenerator.NAME).date(context.properties.nowSupplier()))
      addKDoc(input.documentation)
    }

    val allCommandHandlersInterfaceName = (input.canonicalName.namespace + Name(input.name.value + "AllCommandHandlers")).asClassName()
    val allCommandHandlersInterfaceBuilder = builder.interfaceBuilder(allCommandHandlersInterfaceName).apply {
      addKDoc(Documentation("Union interface for all command handlers"))
      // TODO: introduce scopes in meta, to allow correct grouping, instead of building all command handlers for the context
    }

    /*
    Single interface for each command
     */
    input.protocol.messages
      .filterValues { message -> message.isDecider() || message.isDeciderInit() }
      .entries
      .groupBy { message -> message.value.messageMetaData()?.group?.value ?: UNKNOWN_GROUP }
      .mapNotNull { (groupName, messages) ->

        // named
        if (groupName != UNKNOWN_GROUP) {
          // create one enclosing type for all command handlers
          val groupingTypeName = (input.canonicalName.namespace + Name(groupName.firstUppercase() + "CommandHandlers")).asClassName()
          listOf(
            builder
              .interfaceBuilder(groupingTypeName)
              .apply {
                messages
                  .mapNotNull { (name, message) ->
                    if (message.isDecider()) {
                      buildCommandHandlerFunction(name, message, context.avroPoetTypes)?.let { function ->
                        context.registry.processors.filter(KotlinFunSpecFromProtocolMessageProcessor::class).executeAll(
                          context = context,
                          input = message,
                          builder = function
                        )

                        addFunction(function)
                      }
                    } else {
                      require(message.isDeciderInit()) { "Sanity check failed, expected the message to be a decider init but it was ${message.messageMetaData()?.type}" }
                      val factory = builder.interfaceBuilder((input.canonicalName.namespace + Name(groupName.firstUppercase() + "Factory")).asClassName())
                        .apply {
                          addKDoc(Documentation("Factory for ${groupingTypeName.simpleName}."))
                          buildInitCommandHandlerFunction(name, message, groupingTypeName, context.avroPoetTypes)?.let { function ->
                            context.registry.processors.filter(KotlinFunSpecFromProtocolMessageProcessor::class).executeAll(
                              context = context,
                              input = message,
                              builder = function
                            )

                            addFunction(function)

                          }

                        }
                      addType(factory)
                    }
                  }
              }
          )
        } else {
          messages.mapNotNull { (name, message) ->
            // create type for each command handler
            buildCommandHandlerFunction(name, message, context.avroPoetTypes)?.let { function ->
              val commandHandlerInterfaceName = (input.canonicalName.namespace + Name(name.value.firstUppercase() + "CommandHandler")).asClassName()
              val interfaceBuilder = builder.interfaceBuilder(commandHandlerInterfaceName).apply {
                // TODO: the strategy should be a fall-through in order: on message, on message type, on referenced-type
                addKDoc(message.documentation)
              }
              context.registry.processors.filter(KotlinFunSpecFromProtocolMessageProcessor::class).executeAll(
                context = context,
                input = message,
                builder = function
              )

              interfaceBuilder.addFunction(function) // add function to the interface
            }
          }
        }
      }.flatten().forEach { interfaceBuilder ->
        objectBuilder.addType(interfaceBuilder) // add interface to object
        allCommandHandlersInterfaceBuilder.addSuperinterface(ClassName.bestGuess(interfaceBuilder.spec().className.simpleName))
      }


    objectBuilder.addType(allCommandHandlersInterfaceBuilder) // add union interface
    fileBuilder.addType(objectBuilder)

    // TODO run processors

    return fileBuilder.build()
  }

  private fun buildCommandHandlerFunction(name: Name, message: AvroProtocol.Message, avroPoetTypes: AvroPoetTypes): KotlinFunSpecBuilder? {
    return if (message.request.fields.size == 1) {
      // TODO: the strategy should be a fall-through in order: on message, on message type, on referenced-type
      funBuilder(name.value).apply {
        addModifiers(KModifier.ABSTRACT)
        addAnnotation(CommandHandler::class)
        message.request.fields.forEach { f ->
          this.addParameter(f.name.value, avroPoetTypes[f.schema.hashCode].typeName)
        }
      }
    } else {
      logger.warn { "Skipped command handler definition $name, because it had more then one parameter, but at most one is supported." }
      null
    }
  }

  private fun buildInitCommandHandlerFunction(name: Name, message: AvroProtocol.Message, groupName: ClassName, avroPoetTypes: AvroPoetTypes): KotlinFunSpecBuilder? {
    return if (message.request.fields.size == 1) {
      funBuilder(name.value).apply {
        addModifiers(KModifier.ABSTRACT)
        addAnnotation(CommandHandler::class)
        returns(ClassName.bestGuess(groupName.simpleName))
        message.request.fields.forEach { f ->
          this.addParameter(f.name.value, avroPoetTypes[f.schema.hashCode].typeName)
        }
        addKdoc("Factory command handler initializing ${groupName.simpleName}")
      }
    } else {
      null
    }
  }


  override fun test(context: ProtocolDeclarationContext, input: Any): Boolean {
    return super.test(context, input)
      && input is ProtocolDeclaration
      && input.protocol.messages.values.any { message -> message.isDecider() || message.isDeciderInit() }
  }
}
