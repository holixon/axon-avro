package io.holixon.axon.avro.example.kotlin

import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import io.toolisticon.kotlin.avro.serialization.isKotlinxDataClass
import io.toolisticon.kotlin.avro.serialization.isKotlinxEnumClass
import kotlinx.serialization.Serializable
import org.apache.avro.Schema
import org.axonframework.spring.serialization.avro.ClasspathAvroSchemaLoader
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.util.ClassUtils
import kotlin.reflect.KClass

/**
 * Finds all kotlinx-serializable classes in given packages and extracts schemas.
 */
class KotlinxClasspathAvroSchemaLoader(
  private val avro: AvroKotlinSerialization,
  private val resourceLoader: ResourceLoader
) : ClasspathAvroSchemaLoader {

  companion object {
    fun findKotlinxClasses(packageNames: List<String>, resourceLoader: ResourceLoader): List<KClass<*>> {
      val candidateProvider = ClassPathScanningCandidateComponentProvider(false).apply {
        this.resourceLoader = resourceLoader
        this.addIncludeFilter(AnnotationTypeFilter(Serializable::class.java))
      }

      return packageNames.flatMap { packageName -> candidateProvider.findCandidateComponents(packageName) }
        .map { it.beanClassName!! }
        .map { ClassUtils.forName(it, this::class.java.classLoader).kotlin }
        .filter { it.isKotlinxDataClass() || it.isKotlinxEnumClass() }
    }
  }

  override fun load(packageNames: List<String>): List<Schema> = findKotlinxClasses(packageNames, resourceLoader)
    .map(avro::schema)
    .map { it.get() }
}
