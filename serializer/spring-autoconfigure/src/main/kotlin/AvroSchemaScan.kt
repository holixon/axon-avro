package io.holixon.axon.avro.serializer.spring

import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

/**
 */
@MustBeDocumented
@Import(AvroSchemaPackages.Registrar::class)
annotation class AvroSchemaScan(
  val basePackages: Array<String> = [],
  val basePackageClasses: Array<KClass<*>> = []
)
