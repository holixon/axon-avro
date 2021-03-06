package io.holixon.axon.avro.maven

import io.holixon.axon.avro.maven.PrepareSchemaDeploymentMojo.Companion.GOAL
import io.holixon.axon.avro.maven.avro.verifyAllAvscInRoot
import io.holixon.axon.avro.maven.executor.ReadMeMarkdownGenerator
import io.toolisticon.maven.fn.CleanDirectory
import io.toolisticon.maven.fn.FileExt.append
import io.toolisticon.maven.fn.FileExt.createIfNotExists
import io.toolisticon.maven.mojo.AbstractContextAwareMojo
import io.toolisticon.maven.mojo.RuntimeScopeDependenciesConfigurator
import io.toolisticon.maven.plugin.BuildHelperMavenPlugin
import io.toolisticon.maven.plugin.MavenResourcesPlugin
import io.toolisticon.maven.plugin.MavenResourcesPlugin.CopyResourcesCommand.CopyResource
import io.toolisticon.maven.plugin.ResourceData
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import java.io.File

@Mojo(
  name = GOAL,
  defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
  requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
  configurator = RuntimeScopeDependenciesConfigurator.ROLE_HINT,
  requiresProject = true
)
class PrepareSchemaDeploymentMojo : AbstractContextAwareMojo() {
  companion object {
    const val GOAL = "prepare-schema-deployment"
  }

  @Parameter(
    property = "sourceDirectory",
    defaultValue = "\${project.basedir}/src/main/avro",
    required = false,
    readonly = true
  )
  private lateinit var sourceDirectory: File

  @Parameter(
    property = "markdownGeneration",
    required = false,
    readonly = true
  )
  private val markdownGeneration: MarkdownGenerationConfig = MarkdownGenerationConfig()


  @Parameter(
    property = "targetDirectory",
    required = true,
    readonly = true,
    defaultValue = "\${project.build.directory}/generated-resources/avro"
  )
  private lateinit var targetDirectory: File

  override fun execute() {
    // check avro schema dir exists
    require(this::sourceDirectory.isInitialized && sourceDirectory.isDirectory && sourceDirectory.exists()) { "Source directory '$sourceDirectory' has to be an existing directory" }


    // read and verify all avsc schema files in source directory, this throws exceptions on the first schema that fails
    val schemas = verifyAllAvscInRoot(sourceDirectory)

    // copy schema files to generated resources
    mojoContext.execute(
      MavenResourcesPlugin.CopyResourcesCommand(
        outputDirectory = targetDirectory.createIfNotExists(),
        resources = listOf(
          CopyResource(
            directory = sourceDirectory,
            filtering = false
          )
        )
      )
    )

    // remove .gitkeep and empty directories from generated-sources
    CleanDirectory(
      directory = targetDirectory,
      deleteFiles = setOf(".gitkeep"),
      deleteEmptyDirectories = true
    ).run()

    // add generated-source dir as resource directory, so it ends up in classes
    mojoContext.execute(
      BuildHelperMavenPlugin.AddResourceDirectoryCommand(
        resource = ResourceData(directory = targetDirectory)
      )
    )

    if (markdownGeneration.enable) {
      val readmeFile = requireNotNull( markdownGeneration.resolve(mojoContext.mavenProject).readmeFile)
      ReadMeMarkdownGenerator(
        logger = logger,
        enabled = markdownGeneration.enable,
        readmeFile = readmeFile,
        schemaAndFiles = schemas,
        projectBaseDir = requireNotNull(mojoContext.mavenProject).basedir
      ).run()
    }
  }



  data class MarkdownGenerationConfig(
    var enable : Boolean = false,
    var readmeFile: File? = null,
    var markerStart: String = ReadMeMarkdownGenerator.DEFAULT_START,
    var markerEnd: String = ReadMeMarkdownGenerator.DEFAULT_END
  ) {
    companion object {
      const val DEFAULT_README = "\${project.basedir}/README.md"
    }

    fun resolve(mavenProject: MavenProject?) = if (mavenProject == null || readmeFile != null) this else {
      copy(readmeFile = mavenProject.basedir.append("README.md"))
    }
  }
}
