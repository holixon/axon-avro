package io.holixon.axon.avro.serializer.plugin

import io.axoniq.axonserver.plugin.ConfigurationListener
import io.axoniq.axonserver.plugin.interceptor.ReadEventInterceptor
import io.holixon.axon.avro.serializer.plugin.interceptor.AvroSingleObjectEncodedToJsonReadEventInterceptor
import io.holixon.axon.avro.serializer.plugin.provider.RestClientSingleObjectToJsonConverterProvider
import mu.KLogging
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration

/**
 * Plugin responsible for de-serialization of AVRO messages.
 */
class AxonAvroSerializerPlugin : BundleActivator {

  companion object : KLogging()

  private val registrations = mutableSetOf<ServiceRegistration<*>>()

  override fun start(context: BundleContext) {

    // register config listener to receive configuration changes
    val configurationHandler = AxonAvroSerializerPluginConfigurationListener()
    context.registerService(ConfigurationListener::class.java, configurationHandler, null).also {
      registrations.add(it)
    }

    // register provider for the converter, which is used in the interceptor
    // this provider is currently registered via OSGi service registry
    // we could have passed it directly, but later we might use a different provider
    val provider = RestClientSingleObjectToJsonConverterProvider(configurationHandler)
    context.registerService(SingleObjectToJsonConverterProvider::class.java, provider, null).also {
      registrations.add(it)
    }

    // register event read interceptor for Single Object Encoded to JSON conversion
    val interceptor = AvroSingleObjectEncodedToJsonReadEventInterceptor(configurationHandler)
    context.registerService(ReadEventInterceptor::class.java, interceptor, null).also {
      registrations.add(it)
    }
  }

  override fun stop(context: BundleContext) {
    registrations.forEach { it.unregister() }
  }
}
