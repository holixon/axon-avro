package io.holixon.axon.avro.generation

import _ktx.ResourceKtx.resourceUrl
import io.toolisticon.kotlin.avro.AvroParser
import io.toolisticon.kotlin.avro.declaration.ProtocolDeclaration
import io.toolisticon.kotlin.avro.generator.AvroKotlinGenerator
import io.toolisticon.kotlin.avro.generator.DefaultAvroKotlinGeneratorProperties
import io.toolisticon.kotlin.avro.generator.spi.AvroCodeGenerationSpiRegistry
import java.time.Instant

object TestFixtures {
  val NOW = Instant.parse("2024-08-21T23:19:02.152209Z")
  val NOW_SUPPLER = { NOW }
  val PARSER = AvroParser()

  val DEFAULT_PROPERTIES = DefaultAvroKotlinGeneratorProperties(nowSupplier = NOW_SUPPLER)
  val DEFAULT_GENERATOR = AvroKotlinGenerator(properties = DEFAULT_PROPERTIES)
  val DEFAULT_REGISTRY = AvroCodeGenerationSpiRegistry.load()

  fun parseProtocol(path:String) : ProtocolDeclaration = PARSER.parseProtocol(
    resourceUrl(path)
  )
}
