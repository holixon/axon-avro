package io.holixon.axon.avro.generation.processor

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.holixon.axon.avro.generation.meta.FieldMetaData.Companion.fieldMetaData
import io.holixon.axon.avro.generation.meta.FieldMetaDataType
import io.holixon.axon.avro.generation.support.TargetAggregateIdentifierAnnotation
import io.toolisticon.kotlin.avro.generator.processor.ConstructorPropertyFromRecordFieldProcessorBase
import io.toolisticon.kotlin.avro.generator.spi.SchemaDeclarationContext
import io.toolisticon.kotlin.avro.model.RecordField
import io.toolisticon.kotlin.generation.builder.KotlinConstructorPropertySpecBuilder

// TODO: rename to generic processor to support all Axon specific field annotations
@OptIn(ExperimentalKotlinPoetApi::class)
class AxonTargetIdentifierAnnotationProcessor : ConstructorPropertyFromRecordFieldProcessorBase() {

  override fun invoke(context: SchemaDeclarationContext, input: RecordField, builder: KotlinConstructorPropertySpecBuilder): KotlinConstructorPropertySpecBuilder  = builder.apply {
    requireNotNull(input)
    addAnnotation(TargetAggregateIdentifierAnnotation)
  }

  override fun test(ctx: SchemaDeclarationContext, input: Any): Boolean {
    return super.test(ctx, input) && input is RecordField && input.fieldMetaData()?.type == FieldMetaDataType.Association
  }


}
