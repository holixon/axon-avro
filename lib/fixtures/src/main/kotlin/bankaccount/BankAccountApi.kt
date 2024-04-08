package bankaccount

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.toolisticon.jackson.module.JacksonAvroModule

object BankAccountApi {

  fun configureObjectMapper(objectMapper: ObjectMapper = jacksonObjectMapper()): ObjectMapper = objectMapper.copy().apply {
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    registerKotlinModule()
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    registerModules(JacksonAvroModule())
  }

}
