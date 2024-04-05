package io.holixon.axon.avro.serializer

import com.github.avrokotlin.avro4k.Avro
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test

@Serializable
enum class FindAllQuery {
  INSTANCE
}

internal class Avro4kEnumSerializationTest {



  @Test
  fun name() {
    println(Avro.default.schema(FindAllQuery.serializer()))
  }
}
