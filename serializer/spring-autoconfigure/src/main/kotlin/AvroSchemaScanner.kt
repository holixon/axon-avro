package io.holixon.axon.avro.serializer.spring

import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import kotlinx.serialization.Serializable
import mu.KLogging
import org.apache.avro.specific.SpecificRecordBase
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter

internal class AvroSchemaScanner(
  private val resourceLoader: ResourceLoader,
  private val detectKotlinXSerialization: Boolean,
  private val detectSpecificRecordBase: Boolean,
  private val avro: AvroKotlinSerialization = AvroKotlinSerialization(),
) {

  companion object : KLogging()

  private val candidateProvider = ClassPathScanningCandidateComponentProvider(false).apply {
    resourceLoader = this@AvroSchemaScanner.resourceLoader
    if (detectSpecificRecordBase) {
      addIncludeFilter(AssignableTypeFilter(SpecificRecordBase::class.java))
    }
    if (detectKotlinXSerialization) {
      addIncludeFilter(AnnotationTypeFilter(Serializable::class.java))
    }
  }

  fun scan(packageNames: List<String>): List<AvroSchema> {
    return packageNames.map { packageName -> scan(packageName) }.flatten()
  }

  private fun scan(packageName: String): List<AvroSchema> {
    val candidates = candidateProvider.findCandidateComponents(packageName)
    return candidates.mapNotNull { candidate ->
      logger.trace { "Analyzing candidate: $candidate" }
      try {
        val candidateClass = Class.forName(candidate.beanClassName)
        if (SpecificRecordBase::class.java.isAssignableFrom(candidateClass)) {
          @Suppress("UNCHECKED_CAST")
          val specificRecordClass = candidateClass as Class<SpecificRecordBase>
          val schema = specificRecordClass.getClassSchema()
          logger.info { "Found specific record of type: ${specificRecordClass.name} with schema $schema" }
          schema
        } else if (candidateClass.isAnnotationPresent(Serializable::class.java) && candidateClass.kotlin.isData) {
          val schema = avro.schema(candidateClass.kotlin)
          logger.info { "Found KotlinX Serialized data class ${candidateClass.name} with schema $schema" }
          schema
        } else {
          logger.debug { "Ignoring schema $candidate for $packageName." }
          null
        }
      } catch (e: Exception) {
        // logger.error(e) { "Error for candidate $candidate." }
        // ignore all errors, there might be any reasons for those
        null
      }
    }
  }

  private fun Class<SpecificRecordBase>.getClassSchema(): AvroSchema {
    val schema = this.getMethod("getClassSchema").invoke(null) as org.apache.avro.Schema
    return AvroSchema(schema)
  }

}
