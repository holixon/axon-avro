package io.holixon.axon.avro.serializer.plugin.interceptor

import com.google.protobuf.ByteString
import io.axoniq.axonserver.grpc.SerializedObject
import io.axoniq.axonserver.grpc.event.Event
import io.axoniq.axonserver.plugin.ExecutionContext
import io.axoniq.axonserver.plugin.interceptor.ReadEventInterceptor
import io.holixon.axon.avro.serializer.plugin.AxonAvroSerializerPluginConfigurationListener
import io.holixon.axon.avro.serializer.plugin.ext.data
import io.holixon.axon.avro.serializer.plugin.ext.findSingleObjectToJsonConverterProvider
import io.holixon.axon.avro.serializer.plugin.ext.isDashboardRequest
import io.holixon.axon.avro.serializer.plugin.ext.usingSingleObjectJsonConverterInContext
import io.toolisticon.avro.kotlin.value.ByteArrayValue
import io.toolisticon.avro.kotlin.value.JsonString
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import mu.KLogging
import org.osgi.framework.FrameworkUtil


/**
 * If the event payload is single-object-encoded, replace the payload bytes with the json representation of the event.
 */
class AvroSingleObjectEncodedToJsonReadEventInterceptor(configurationHandler: AxonAvroSerializerPluginConfigurationListener) :
  ReadEventInterceptor {

  companion object : KLogging()

  override fun readEvent(event: Event, executionContext: ExecutionContext): Event {

    if (!executionContext.isDashboardRequest()) {
      logger.trace { "Request is not coming from dashboard, just passing event: ${event.messageIdentifier}" }
      return event
    }

    val payloadBytes = event.payload.data.toByteArray()

    return try {
      val singleObjectEncodedBytes = SingleObjectEncodedBytes(ByteArrayValue(payloadBytes))

      logger.trace { "Event: $event" }
      logger.trace { "ExecutionContextData: ${executionContext.data()}" }

      try {
        val bundleContext = FrameworkUtil.getBundle(this.javaClass).bundleContext

        bundleContext.usingSingleObjectJsonConverterInContext(
          contextName = executionContext.contextName(),
          serviceReference = bundleContext.findSingleObjectToJsonConverterProvider()
        ) { jsonConverter ->

          val jsonConverted: JsonString = jsonConverter.convert(singleObjectEncodedBytes)
          logger.trace { "Converted JSON: $jsonConverted" }
          val result = Event.newBuilder(event)
            .setPayload(
              SerializedObject.newBuilder(event.payload)
                .setData(ByteString.copyFrom(jsonConverted.value, Charsets.UTF_8))
                .build()
            )
            .build()
          logger.trace { "Resulting event is $result" }
          result
        }
      } catch (e: Exception) {
        logger.trace { "Could not convert: ${e.message}\n${e.stackTraceToString()}" }
        event
      }

    } catch (e: Exception) {
      logger.trace { "Not converting, because not single object encoded." }
      event
    }
  }
}
