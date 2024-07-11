package bank

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("jackson")
@Configuration
class JacksonSerializerConfiguration {
}
