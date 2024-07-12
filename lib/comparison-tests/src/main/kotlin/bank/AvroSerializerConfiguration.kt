package bank

import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.EnableAxonAvroSerializer
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("avro")
@EnableAxonAvroSerializer
class AvroSerializerConfiguration {

  @Bean
  @Primary
  fun defaultSerializer(): Serializer = JacksonSerializer.builder().build()

  @Bean
  @Qualifier("eventSerializer")
  fun eventSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  @Qualifier("messageSerializer")
  fun messageSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  fun schemaResolver() = BankAccountSchemas.schemaResolver

}