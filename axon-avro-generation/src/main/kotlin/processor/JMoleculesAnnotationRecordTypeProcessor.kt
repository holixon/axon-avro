package io.holixon.axon.avro.generation.processor

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.holixon.axon.avro.generation.meta.RecordMetaData.Companion.recordMetaData
import io.holixon.axon.avro.generation.meta.RecordMetaDataType
import io.holixon.axon.avro.generation.meta.RecordMetaDataType.Command
import io.holixon.axon.avro.generation.meta.RecordMetaDataType.Event
import io.holixon.axon.avro.generation.meta.RecordMetaDataType.Query
import io.holixon.axon.avro.generation.meta.RecordMetaDataType.QueryResult
import io.holixon.axon.avro.generation.meta.RecordMetaDataType.Undefined
import io.holixon.axon.avro.generation.support.JMoleculesAnnotationSupplier.CommandAnnotation
import io.holixon.axon.avro.generation.support.JMoleculesAnnotationSupplier.DomainEventAnnotation
import io.toolisticon.kotlin.avro.generator.processor.KotlinDataClassFromRecordTypeProcessorBase
import io.toolisticon.kotlin.avro.generator.spi.SchemaDeclarationContext
import io.toolisticon.kotlin.avro.model.RecordType
import io.toolisticon.kotlin.generation.builder.KotlinDataClassSpecBuilder

@OptIn(ExperimentalKotlinPoetApi::class)
class JMoleculesAnnotationRecordTypeProcessor : KotlinDataClassFromRecordTypeProcessorBase() {

  private fun recordMetaDataType(input: Any): RecordMetaDataType = if (input is RecordType) {
    input.recordMetaData()?.type ?: Undefined
  } else {
    Undefined
  }

  override fun invoke(
    context: SchemaDeclarationContext,
    input: RecordType,
    builder: KotlinDataClassSpecBuilder
  ): KotlinDataClassSpecBuilder = builder.apply {
    when (recordMetaDataType(input)) {
      Event -> DomainEventAnnotation(input.canonicalName).addIfEnabled(this)
      Command -> CommandAnnotation(input.canonicalName).addIfEnabled(this)
      Query, QueryResult, Undefined -> {
      }
    }
  }

  override fun test(ctx: SchemaDeclarationContext, input: Any): Boolean {
    return super.test(ctx, input) && Undefined != recordMetaDataType(input)
  }
}
