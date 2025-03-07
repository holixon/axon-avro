package io.holixon.axon.avro.example.kotlin

import io.holixon.axon.avro.serializer.AvroKotlinSerializerStrategy
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import org.axonframework.serialization.RevisionResolver
import org.axonframework.spring.serialization.avro.AvroSchemaPackages
import org.axonframework.spring.serialization.avro.AvroSchemaScan
import org.springframework.beans.factory.BeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader

fun main(vararg args: String) {
  System.setProperty("disable-axoniq-console-message", "true")
  runApplication<AxonAvroKotlinExampleApplication>(*args)
}

@SpringBootApplication
@AvroSchemaScan(basePackages = ["holi.bank"])
class AxonAvroKotlinExampleApplication {

  @Bean
  fun avroKotlinSerialization(beanFactory: BeanFactory, resourceLoader: ResourceLoader): AvroKotlinSerialization {
    val packageNames = AvroSchemaPackages.get(beanFactory).packages

    val kotlinxClasses = KotlinxClasspathAvroSchemaLoader.findKotlinxClasses(packageNames, resourceLoader)

    return AvroKotlinSerialization.configure().apply {
      kotlinxClasses.forEach(this::schema)
    }
  }

  @Bean
  fun kotlinxStrategy(avroKotlinSerialization: AvroKotlinSerialization, revisionResolver: RevisionResolver): AvroKotlinSerializerStrategy {
    return AvroKotlinSerializerStrategy(
      avro = avroKotlinSerialization,
      revisionResolver = revisionResolver
    )
  }
}
