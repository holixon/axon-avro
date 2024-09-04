package bank

import bankaccount.BankAccountApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("jackson")
@Configuration
class JacksonSerializerConfiguration {
  @Bean
  @Primary
  fun objectMapper() = BankAccountApi.configureObjectMapper()
}
