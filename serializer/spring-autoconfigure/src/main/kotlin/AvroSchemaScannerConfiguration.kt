package io.holixon.axon.avro.serializer.spring

import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import io.toolisticon.kotlin.avro.repository.AvroSchemaResolver
import org.springframework.beans.factory.BeanFactory
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class AvroSchemaScannerConfiguration {

  @Bean
  @ConditionalOnMissingBean
  fun getSchemas(beanFactory: BeanFactory, resourceLoader: ResourceLoader): List<AvroSchema> {
    val packages = AvroSchemaPackages.get(beanFactory).getPackageNames()
    val packagesToScan = if (packages.isEmpty() && AutoConfigurationPackages.has(beanFactory)) {
      AutoConfigurationPackages.get(beanFactory).toList()
    } else {
      packages
    }
    return AvroSchemaScanner(
      detectKotlinXSerialization = true, // FIXME
      detectSpecificRecordBase = true, // FIXME
      resourceLoader = resourceLoader
    ).scan(packagesToScan)
  }

  @Bean
  @ConditionalOnMissingBean
  fun defaultAvroSchemaResolver(schemas: List<AvroSchema>): AvroSchemaResolver {
    require(schemas.isNotEmpty()) { "Could not find any Avro Schemas. At least one schema is required for the resolver." }
    return io.toolisticon.kotlin.avro.repository.avroSchemaResolver(schemas)
  }

}
