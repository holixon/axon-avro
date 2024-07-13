If you are using Spring Boot, please add the following dependency to your Maven pom:

```xml

<dependency>
  <groupId>io.holixon.axon.avro</groupId>
  <artifactId>axon-avro-serializer-spring-autoconfigure</artifactId>
  <version>${axon-avro.version}</version>
</dependency>

```

To activate the serializer please add the following to your configuration:

```kotlin 

@Configuration
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

```
