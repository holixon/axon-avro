package bank

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("avro")
@Configuration
class AvroSerializerConfiguration {
}
