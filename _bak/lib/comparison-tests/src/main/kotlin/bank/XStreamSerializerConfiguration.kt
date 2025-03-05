package bank

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Profile("xstream")
@Configuration
class XStreamSerializerConfiguration {

  @Bean
  fun xStream() = XStream().also {
    it.addPermission(AnyTypePermission())
  }
}
