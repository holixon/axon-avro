package io.holixon.axon.avro.serializer.strategy

import io.holixon.axon.avro.serializer.converter.SingleObjectEncodedToGenericRecordConverter
import io.toolisticon.avro.kotlin.AvroKotlin
import io.toolisticon.avro.kotlin.avroSchemaResolver
import io.toolisticon.avro.kotlin.value.HexString
import io.toolisticon.avro.kotlin.value.SingleObjectEncodedBytes
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.junit.jupiter.api.Test
import java.util.*


internal class MetaDataStrategyTest {

  private val strategy = MetaDataStrategy(AvroKotlin.defaultLogicalTypeConversions.genericData)

  @Test
  fun `verify metaData is schema compliant`() {
    val meta =  MetaData.from(mapOf(
      "traceId" to UUID.randomUUID().toString(),
      "correlationId" to UUID.randomUUID().toString()
    ))

    assertThat(strategy.isSchemaCompliant(meta)).isTrue()
  }

  @Test
  fun display() {
    val byteString = "c301c1e29b0067fe8730040e74726163654964044832396565646532642d306463622d343630622d613831652d3236636436353535643635641a636f7272656c6174696f6e4964044832396565646532642d306463622d343630622d613831652d32366364363535356436356400"
      .uppercase()
      .chunked(2).joinToString(separator = " ", prefix = "[", postfix = "]")
    val soe = SingleObjectEncodedBytes(hex = HexString(byteString))

    println(SingleObjectEncodedToGenericRecordConverter(avroSchemaResolver(MetaDataStrategy.SCHEMA)).convert(soe).toString())

  }
}
