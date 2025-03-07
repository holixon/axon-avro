package io.holixon.axon.avro.serializer.spring

import io.toolisticon.kotlin.avro.repository.AvroSchemaResolver
import org.apache.avro.message.MissingSchemaException
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
  path = ["\${axon.avro.serializer.rest-base-path:#{T(io.holixon.axon.avro.serializer.spring.AxonAvroSerializerProperties).DEFAULT_REST_BASE_PATH}}"],
  produces = [org.springframework.http.MediaType.APPLICATION_JSON_VALUE]
)
class AvroSchemaResolverResource(
  private val schemaResolver: AvroSchemaResolver
) {
  @GetMapping(path = ["/{fingerprint}"])
  fun getSchema(@PathVariable("fingerprint") fingerprint: Long): ResponseEntity<String> {
    return try {
      ok(schemaResolver.findByFingerprint(fingerprint).toString())
    } catch (e: MissingSchemaException) {
      notFound().build()
    }
  }
}
