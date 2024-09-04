## Features

The Axon Avro Serializer can be used as a serializer in your application using Axon Framework. In doing so, it will serialize and de-serialize 
Axon Framework messages (commands, command response, events, queries, query responses) using Apache Avro Single Object Encoded format. In case you use 
Axon Server as Event store, the payload events will be stored in Apache Avro on disk. If you are using a different store, it will store the payload 
of events in the event store respectively.

The serializer supports classes created using Apache Avro Java Generator (Maven Plugin) and Kotlin Data classes compatible with KotlinX Serialization. 
The serializer requires a schema resolver to operate. The latter must contain schemas of all messages and classes which are about to be serialized.

## Limitations / Out of scope

Currently, we believe that Axon Messages should be serialized using Apache Avro. Axon Framework serializes some other data too. For example 
Tokens, Snapshots, Sagas are also serialized, but we believe that usage of Axon Avro this has limited / no value and therefor no support for 
this is provided.

As a practical implication, you can use Avro Axon Serializer to serialize messages and events, but can't use it a sa default serializer in 
your application.

## How to use in a SpringBoot project

In order to use the serializer in a SpringBoot project, you will need to add the following dependency to your project:

```xml
<dependency>
  <groupId>io.holixon.axon.avro</groupId>
  <artifactId>axon-avro-serializer-spring-autoconfigure</artifactId>
  <version>${axon-avro-serializer.version}</version>
</dependency>
```

On a configuration of your SpringBoot application please provide the following Bean factories:

```kotlin
@Configuration
@EnableAxonAvroSerializer
class AvroNoServerConfiguration {
  
  @Bean
  @Qualifier("eventSerializer")
  fun eventSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  @Qualifier("messageSerializer")
  fun messageSerializer(builder: AvroSerializer.Builder): Serializer = builder.build()

  @Bean
  fun schemaResolver(): AvroSchemaResolver {
    // return a schema resolver.
    val schemas = listOf(
      AvroSchema(avro4k.schema(MyKotlinXEvent.serializer())) 
    )
    return avroSchemaResolver(schemas)
  }
}
```

By doing so, you configure the message and event serializers to be Axon Avro serializers.


## How to use without SpringBoot

// TODO -> describe usage

```xml
<dependency>
  <groupId>io.holixon.axon.avro</groupId>
  <artifactId>axon-avro-serializer-spring-core</artifactId>
  <version>${axon-avro-serializer.version}</version>
</dependency>
```
