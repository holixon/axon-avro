package io.holixon.axon.avro.generation.strategy

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.holixon.axon.avro.generation.meta.RecordMetaData.Companion.recordMetaData
import io.holixon.axon.avro.generation.meta.RecordMetaDataType
import io.toolisticon.kotlin.avro.declaration.ProtocolDeclaration
import io.toolisticon.kotlin.avro.generator.AvroKotlinGenerator
import io.toolisticon.kotlin.avro.generator.addKDoc
import io.toolisticon.kotlin.avro.generator.asClassName
import io.toolisticon.kotlin.avro.generator.rootClassName
import io.toolisticon.kotlin.avro.generator.spi.ProtocolDeclarationContext
import io.toolisticon.kotlin.avro.generator.strategy.AvroFileSpecFromProtocolDeclarationStrategy
import io.toolisticon.kotlin.avro.model.RecordType
import io.toolisticon.kotlin.avro.value.Documentation
import io.toolisticon.kotlin.avro.value.Name
import io.toolisticon.kotlin.generation.KotlinCodeGeneration
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.buildFile
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.buildInterface
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.builder.objectBuilder
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.className
import io.toolisticon.kotlin.generation.spec.KotlinFileSpec
import io.toolisticon.kotlin.generation.support.GeneratedAnnotation
import org.axonframework.eventhandling.EventHandler

@OptIn(ExperimentalKotlinPoetApi::class)
class AxonEventHandlerInterfaceStrategy : AvroFileSpecFromProtocolDeclarationStrategy() {
  override fun invoke(context: ProtocolDeclarationContext, input: ProtocolDeclaration): KotlinFileSpec {
    val fileName: ClassName = (input.canonicalName.namespace + Name(input.name.value + "EventHandlers")).asClassName()
    val builder = objectBuilder(fileName).apply {
      addAnnotation(GeneratedAnnotation(value = AvroKotlinGenerator.NAME).date(context.properties.nowSupplier()))
      addKDoc(input.documentation)
    }

    val allEvents = input.protocol.recordTypes.filterIsInstance<RecordType>()
      .filter { RecordMetaDataType.Event == it.recordMetaData()?.type }.map { it.hashCode }

    val allEventsInterfaceName = (input.canonicalName.namespace + Name(input.name.value + "AllEventHandlers")).asClassName()
    val allEventsInterfaceBuilder = KotlinCodeGeneration.builder.interfaceBuilder(allEventsInterfaceName).apply {
      addKDoc(Documentation("Union interface for all event handlers."))
    }

    allEvents.map { context.avroPoetTypes[it] }.map {
      val eventName = it.avroType.name
      val interfaceName : ClassName= className( fileName.packageName, it.avroType.name.suffix("EventHandler").value)
      buildInterface(interfaceName) {
        // TODO what doc to use? hierarchy?
        addFunction("on" + eventName.value) {
          addAnnotation(EventHandler::class)
          makeAbstract()
          addParameter("event", it.suffixedTypeName)
        }
      }
    }.forEach {
      builder.addType(it)
      allEventsInterfaceBuilder.addSuperinterface(ClassName.bestGuess(it.spec().className.simpleName))
    }
    builder.addType(allEventsInterfaceBuilder)

    return buildFile(fileName) {
      addType(builder)
      // TODO: file processors
    }
  }
}
