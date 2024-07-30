package io.holixon.axon.avro.serializer.spring.itest.upcaster

import io.holixon.axon.avro.serializer.spring.TestFixtures.DummyEvents
import io.toolisticon.kotlin.avro.value.AvroSchemaCompatibilityMap
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


internal class DummyEventsTest {

//
//  @Test
//  internal fun `schema 01 and 10 are incompatibly`() {
//    val record = GenericData.Record(DummyEvents.SCHEMA_EVENT_01).apply {
//      put("value01", "foo")
//    }
//
//    assertThatThrownBy { decoder.decode<DummyEvent>(encoder.encode(record)) }
//      .isInstanceOf(IllegalArgumentException::class.java)
//      .hasMessageContaining("[READER_FIELD_MISSING_DEFAULT_VALUE]")
//
//  }
}
