package io.holixon.axon.avro.serializer.plugin.provider

import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginProperties
import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginPropertiesForContextResolver
import io.holixon.axon.avro.serializer.plugin.SingleObjectToJsonConverterProvider
import io.toolisticon.avro.kotlin.AvroKotlin.defaultLogicalTypeConversions
import io.toolisticon.avro.kotlin.codec.SingleObjectToJsonConverter
import io.toolisticon.avro.kotlin.model.wrapper.AvroSchema
import io.toolisticon.avro.kotlin.value.AvroFingerprint
import io.toolisticon.avro.kotlin.value.JsonString
import org.apache.avro.generic.GenericData
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap

/**
 * A caching converter resolver using a remote REST resolver and caching all schemas locally.
 */
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
      },
      genericData = defaultLogicalTypeConversions.genericData
    )
  }

  /**
   * Retrieves the schema from the external resource.
   * @param properties properties for schema resolution.
   * @param fingerprint fingerprint of the Avro Schema.
   * @return Avro Schema
   */
  @Throws(IllegalStateException::class)
  private fun getSchema(properties: AxonAvroSerializerPluginProperties, fingerprint: AvroFingerprint): AvroSchema {
    val uri = properties.buildRegistryUri(fingerprint.value)

    val request = HttpRequest
      .newBuilder()
      .uri(uri)
      .GET()
      .headers("Accept", "application/json")
      .build()

    val httpClient: HttpClient = HttpClient.newHttpClient()

    return try {
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

      require(response.statusCode() == 200) { "Expected response to be 200 but got ${response.statusCode()} calling registry at ${uri.toASCIIString()}" }
      val contentTypeHeader = response.headers().firstValue("Content-Type").get()
      require(contentTypeHeader == "application/json") { "Expected application/json content type, but got $contentTypeHeader" }

      AvroSchema(JsonString(response.body()))
    } catch (e: Exception) {
      throw IllegalStateException("Error while retrieving Avro Schema from registry: $uri", e)
    }
  }
}
