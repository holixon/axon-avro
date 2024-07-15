@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package io.holixon.axon.avro.serializer.spring.itest.scan

import io.holixon.axon.avro.serializer.AvroSerializer
import io.holixon.axon.avro.serializer.spring.AvroSchemaScan
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration.Companion.EVENT_SERIALIZER
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerConfiguration.Companion.MESSAGE_SERIALIZER
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerSpringBase
import io.holixon.axon.avro.serializer.spring.AxonAvroSerializerSpringBase.PROFILE_ITEST
import io.holixon.axon.avro.serializer.spring._test.BankTestApplication
import io.holixon.axon.avro.serializer.spring._test.BankTestApplication.PROFILES
import io.toolisticon.kotlin.avro.model.wrapper.AvroSchema
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.serialization.Serializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest(classes = [BankTestApplication::class], webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles(PROFILE_ITEST, PROFILES.AVRO_NO_SERVER_WITHOUT_SCAN)
@ContextConfiguration(initializers = [AvroSchemaScanConfigurationITestBase.Initializer::class])
abstract class AvroSchemaScanConfigurationITestBase {
  companion object : KLogging()

  internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
      System.setProperty("disable-axoniq-console-message", "true")
    }
  }

  @Autowired
  @Qualifier(EVENT_SERIALIZER)
  lateinit var eventSerializer: Serializer

  @Autowired
  @Qualifier(MESSAGE_SERIALIZER)
  lateinit var messageSerializer: Serializer

  @BeforeEach
  internal fun ensure_serializer() {
    assertThat(eventSerializer).isInstanceOf(AvroSerializer::class.java)
    assertThat(messageSerializer).isInstanceOf(AvroSerializer::class.java)
  }

  @AvroSchemaScan(basePackages = ["test.fixture", "test.fixture2"])
  class MultiPackageScanningITest : AvroSchemaScanConfigurationITestBase() {

    @Autowired
    lateinit var avroSchemas: List<AvroSchema>

    @Test
    fun `should find schemas in packages by name`() {
      assertThat(avroSchemas).isNotNull
      assertThat(avroSchemas).hasSize(2) // both generated
    }
  }

  @ContextConfiguration(
    classes = [
      MultiAnnotationsScanningITest.TestConfiguration1::class,
      MultiAnnotationsScanningITest.TestConfiguration2::class
    ]
  )
  class MultiAnnotationsScanningITest : AvroSchemaScanConfigurationITestBase() {

    @AvroSchemaScan(basePackages = ["test.fixture"])
    @Configuration
    class TestConfiguration1

    @AvroSchemaScan(basePackages = ["test.fixture2"])
    @Configuration
    class TestConfiguration2

    @Autowired
    lateinit var avroSchemas: List<AvroSchema>

    @Test
    fun `should find schemas in packages by class mixing kotlinx with specific record base`() {
      assertThat(avroSchemas).isNotNull
      assertThat(avroSchemas).hasSize(2) // both generated
    }
  }

  @AvroSchemaScan(basePackageClasses = [DummyEventWithMoney::class])
  class PackageClassesWithMixedTypesScanningITest : AvroSchemaScanConfigurationITestBase() {

    @Autowired
    lateinit var avroSchemas: List<AvroSchema>

    @Test
    fun `should find schemas in packages by class mixing kotlinx with specific record base`() {
      assertThat(avroSchemas).isNotNull
      assertThat(avroSchemas).hasSize(3) // one generated two from DummyEvents.kt
    }
  }

  @AvroSchemaScan
  class ImplicitPackageClassesWithMixedTypesScanningITest : AvroSchemaScanConfigurationITestBase() {

    @Autowired
    lateinit var avroSchemas: List<AvroSchema>

    @Test
    fun `should find schemas in packages by class mixing kotlinx with specific record base`() {
      assertThat(avroSchemas).isNotNull
      assertThat(avroSchemas).hasSize(3) // one generated two from DummyEvents.kt
    }
  }

  @AvroSchemaScan(basePackageClasses = [AxonAvroSerializerSpringBase::class])
  class NestedPackageClassesWithMixedTypesScanningITest : AvroSchemaScanConfigurationITestBase() {

    @Autowired
    lateinit var avroSchemas: List<AvroSchema>

    @Test
    fun `should find schemas in packages by class mixing kotlinx with specific record base`() {
      assertThat(avroSchemas).isNotNull
      assertThat(avroSchemas).hasSize(3) // one generated two from DummyEvents.kt
    }
  }

}
