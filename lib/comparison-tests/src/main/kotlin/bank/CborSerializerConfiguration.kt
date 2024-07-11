package bank

import bankaccount.BankAccountApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("cbor")
@Configuration
class CborSerializerConfiguration {
  @Bean
  @Primary
  fun objectMapper() = BankAccountApi.configureObjectMapper()
}
