package io.holixon.axon.avro.generation.support

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.buildAnnotation
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.format.FORMAT_STRING
import io.toolisticon.kotlin.generation.spec.KotlinAnnotationSpec
import io.toolisticon.kotlin.generation.spec.KotlinAnnotationSpecSupplier
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.serialization.Revision

@OptIn(ExperimentalKotlinPoetApi::class)
data class RevisionAnnotation(
  val value: String,
) : KotlinAnnotationSpecSupplier {
  override fun spec(): KotlinAnnotationSpec = buildAnnotation(Revision::class) {
    addMember(FORMAT_STRING, value)
  }
}

@OptIn(ExperimentalKotlinPoetApi::class)
data object TargetAggregateIdentifierAnnotation : KotlinAnnotationSpecSupplier {
  override fun spec(): KotlinAnnotationSpec = buildAnnotation(TargetAggregateIdentifier::class)
}
