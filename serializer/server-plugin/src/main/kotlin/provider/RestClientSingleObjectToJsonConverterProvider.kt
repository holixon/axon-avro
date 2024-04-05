package io.holixon.axon.avro.serializer.plugin.provider

import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginProperties
import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginPropertiesForContextResolver
import io.holixon.axon.avro.serializer.plugin.SingleObjectToJsonConverter
import io.holixon.axon.avro.serializer.plugin.SingleObjectToJsonConverterProvider
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.value.AvroFingerprint
import io.toolisticon.avro.kotlin.value.JsonString
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap

class RestClientSingleObjectToJsonConverterProvider(
  private val axonAvroSerializerPluginPropertiesForContextResolver: AxonAvroSerializerPluginPropertiesForContextResolver
) : SingleObjectToJsonConverterProvider {

  private val cache: MutableMap<AvroFingerprint, AvroSchema> = ConcurrentHashMap()

  override fun get(contextName: String): SingleObjectToJsonConverter {
    return SingleObjectToJsonConverter(
      avroSchemaResolver = { fingerprint: AvroFingerprint ->
        cache.computeIfAbsent(fingerprint) { key ->
          val properties = axonAvroSerializerPluginPropertiesForContextResolver.getAxonAvroProperties(contextName)
          getSchema(properties, key)
        }
      }
    )
  }

  private fun getSchema(properties: AxonAvroSerializerPluginProperties, fingerprint: AvroFingerprint): AvroSchema {
    val uri = properties.buildRegistryUri(fingerprint.value)
    val request = HttpRequest
      .newBuilder()
      .uri(uri)
      .GET()
      .build()

    val httpClient: HttpClient = HttpClient.newHttpClient()
    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    require(response.statusCode() == 200) { "Expected response to be 200 but got ${response.statusCode()} calling registry at ${uri.toASCIIString()}" }
    require(
      response.headers().firstValue("Content-Type").get() == "application/json"
    ) { "Expected application/json content type, but got ${response.headers().firstValue("Content-Type").get()}" }

    return AvroSchema(JsonString(response.body()))
  }
}
