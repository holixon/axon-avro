package io.holixon.axon.avro.generation

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.toolisticon.kotlin.avro.generator.AvroKotlinGenerator
import io.toolisticon.kotlin.avro.generator.spi.AvroCodeGenerationSpiRegistry
import io.toolisticon.kotlin.generation.spec.KotlinFileSpecList
import io.toolisticon.kotlin.generation.test.KotlinCodeGenerationTest
import io.toolisticon.kotlin.generation.test.model.KotlinCompilationCommand
import io.toolisticon.kotlin.generation.test.model.requireOk
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@OptIn(ExperimentalKotlinPoetApi::class, ExperimentalCompilerApi::class)
class BankAccountProtocolGeneratorTest {
  companion object : KLogging()

  private val declaration = TestFixtures.parseProtocol("BankAccountProtocol.avpr")
  private val outputDirectory = generatedTestSourcesDirectory()
  private var files: KotlinFileSpecList = KotlinFileSpecList.EMPTY

  @BeforeEach
  fun generate() {
    val spi = AvroCodeGenerationSpiRegistry.load()
    val generator = AvroKotlinGenerator(registry = spi, properties = TestFixtures.DEFAULT_PROPERTIES)

    files = KotlinFileSpecList(generator.generate(declaration))
    files.forEach {
      val written = it.get().writeTo(outputDirectory)
      logger.info("Generated file://${written.absolutePath}")
    }
  }

  @Test
  fun `should generate`() {
    assertThat(files).isNotEmpty
  }

  @Test
  fun `should compile generated code`() {
    println("Compiling....")
    val result = KotlinCodeGenerationTest.compile(
      KotlinCompilationCommand(files)
    )
    println("Compilation ready.")
    result.requireOk()
    /*
    result.result.messages.lines()
      .filter { line -> line.endsWith(".kt") }
      .distinct()
      .forEach { message -> println("file://$message") }
     */
  }

  private fun generatedTestSourcesDirectory(): File {
    val relPath = javaClass.protectionDomain.codeSource.location.file
    val targetDir = File("$relPath../generated-test-sources/avro-kotlin").canonicalFile
    if (!targetDir.exists()) {
      targetDir.mkdir()
    }
    return targetDir
  }

}
