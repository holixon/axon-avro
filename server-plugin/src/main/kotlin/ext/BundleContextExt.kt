package io.holixon.axon.avro.serializer.plugin.ext

import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPlugin
import io.holixon.axon.avro.serializer.plugin.SingleObjectToJsonConverterProvider
import io.toolisticon.kotlin.avro.codec.SingleObjectToJsonConverter
import org.osgi.framework.BundleContext
import org.osgi.framework.InvalidSyntaxException
import org.osgi.framework.ServiceReference

/**
 * Find existing implementations of the SPI to access the schema-based JSON converters and return one.
 */
fun BundleContext.findSingleObjectToJsonConverterProvider(): ServiceReference<SingleObjectToJsonConverterProvider> {
  val serviceCandidates = try {
    getServiceReferences(SingleObjectToJsonConverterProvider::class.java, null)
  } catch (e: InvalidSyntaxException) {
    throw IllegalArgumentException("Error initializing Avro Schema Registry", e)
  } catch (e: IllegalStateException) {
    throw IllegalArgumentException("Error initializing Avro Schema Registry", e)
  }
  return when (serviceCandidates.size) {
    0 -> throw IllegalArgumentException("Could not find any Avro Registry Provider, please install and configure at least one.")
    1 -> serviceCandidates.first()
    else -> {
      AxonAvroSerializerPlugin.logger.warn { "More than one Avro Registry Provider found, taking the first one." }
      serviceCandidates.forEach {
        AxonAvroSerializerPlugin.logger.warn {
          "Provider bundle: ${it.bundle}, provider class: ${it.javaClass.name}"
        }
      }
      serviceCandidates.first()
    }
  }
}

/**
 * Operation run using single object JSON converter.
 */
typealias SingleObjectConverterOperation<T> = (SingleObjectToJsonConverter) -> T

/**
 * Scoped execution of the operation using the SingleObjectJsonConverter provided by the service in given Axon Context.
 */
inline fun <reified T : Any> BundleContext.usingSingleObjectJsonConverterInContext(
  contextName: String,
  serviceReference: ServiceReference<SingleObjectToJsonConverterProvider>,
  serviceWorker: SingleObjectConverterOperation<T>
): T {
  try {
    val provider = getService(serviceReference).also {
      AxonAvroSerializerPlugin.logger.info { "Using $it" }
    }
    return serviceWorker.invoke(provider.get(contextName).also {
      AxonAvroSerializerPlugin.logger.info { "Converter received: $it" }
    })
  } finally {
    ungetService(serviceReference)
  }
}
