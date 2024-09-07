package io.holixon.axon.avro.generation.support

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.buildAnnotation
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.format.FORMAT_STRING
import io.toolisticon.kotlin.generation.spec.KotlinAnnotationSpec
import io.toolisticon.kotlin.generation.spec.KotlinAnnotationSpecSupplier
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.serialization.Revision

@OptIn(ExperimentalKotlinPoetApi::class)
object AxonFrameworkAnnotations {

  data class RevisionAnnotation(
    val value: String,
  ) : KotlinAnnotationSpecSupplier {
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(Revision::class) {
      addMember(FORMAT_STRING, value)
    }
  }

  data object TargetAggregateIdentifierAnnotation : KotlinAnnotationSpecSupplier {
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(TargetAggregateIdentifier::class)
  }
}
