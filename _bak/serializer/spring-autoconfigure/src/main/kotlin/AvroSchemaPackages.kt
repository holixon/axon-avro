package io.holixon.axon.avro.serializer.spring

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.Assert
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils

/**
 * Collects packages to scan for Avro schemas (also multiple).
 * Influenced by [EntityScan].
 */
internal class AvroSchemaPackages(vararg packageNames: String) {

  companion object {
    private val NONE: AvroSchemaPackages = AvroSchemaPackages()
    private val BEAN: String = AvroSchemaPackages::class.java.name

    /**
     * Register the specified avro schema scan packages with the system.
     * @param registry the source registry
     * @param packageNames the package names to register
     */
    @JvmStatic
    fun register(registry: BeanDefinitionRegistry, vararg packageNames: String) {
      Assert.notNull(registry, "Registry must not be null")
      Assert.notNull(packageNames, "PackageNames must not be null")
      register(registry, packageNames.toSet())
    }

    /**
     * Register the specified avro schema scan packages with the system.
     * @param registry the source registry
     * @param packageNames the package names to register
     */
    @JvmStatic
    fun register(registry: BeanDefinitionRegistry, packageNames: Set<String>) {
      Assert.notNull(registry, "Registry must not be null")
      Assert.notNull(packageNames, "PackageNames must not be null")
      if (registry.containsBeanDefinition(BEAN)) {
        val beanDefinition = registry.getBeanDefinition(BEAN) as AvroSchemaScanPackagesBeanDefinition
        beanDefinition.addPackageNames(packageNames)
      } else {
        registry.registerBeanDefinition(BEAN, AvroSchemaScanPackagesBeanDefinition(packageNames))
      }
    }

    /**
     * Return the [AvroSchemaPackages] for the given bean factory.
     * @param beanFactory the source bean factory
     * @return the [AvroSchemaPackages] for the bean factory (never `null`)
     */
    @JvmStatic
    fun get(beanFactory: BeanFactory): AvroSchemaPackages {
      // Currently we only store a single base package, but we return a list to
      // allow this to change in the future if needed
      return try {
        beanFactory.getBean(BEAN, AvroSchemaPackages::class.java)
      } catch (ex: NoSuchBeanDefinitionException) {
        NONE
      }
    }
  }

  private val packageNames: List<String> = packageNames.filter { StringUtils.hasText(it) }.toList()


  /**
   * Return the package names specified from all [@AvroSchemaScan] annotations.
   * @return the avro schema scan package names
   */
  fun getPackageNames(): List<String> {
    return this.packageNames
  }

  /**
   * [ImportBeanDefinitionRegistrar] to store the base package from the importing
   * configuration.
   */
  class Registrar internal constructor(private val environment: Environment) : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(metadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
      register(registry, getPackagesToScan(metadata))
    }

    private fun getPackagesToScan(metadata: AnnotationMetadata): Set<String> {
      val attributes = requireNotNull(AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(AvroSchemaScan::class.java.name)))
      val basePackagesToScan = attributes.getStringArray(AvroSchemaScan::basePackages.name).map { basePackage ->
        StringUtils.tokenizeToStringArray(
          environment.resolvePlaceholders(basePackage),
          ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS
        ).toList()
      }.flatten()

      val baseClassPackagesToScan = attributes.getClassArray(AvroSchemaScan::basePackageClasses.name).map { basePackageClass ->
        environment.resolvePlaceholders(ClassUtils.getPackageName(basePackageClass))
      }

      val packagesToScan: List<String> = basePackagesToScan + baseClassPackagesToScan

      if (packagesToScan.isEmpty()) {
        val packageName = ClassUtils.getPackageName(metadata.className)
        Assert.state(StringUtils.hasLength(packageName), "@${AvroSchemaScan::class.simpleName} cannot be used with the default package")
        return setOf(packageName)
      }
      return packagesToScan.toSet()
    }
  }

  class AvroSchemaScanPackagesBeanDefinition internal constructor(packageNames: Collection<String>) : GenericBeanDefinition() {
    private val packageNames: MutableSet<String> = LinkedHashSet()

    init {
      setBeanClass(AvroSchemaPackages::class.java)
      role = ROLE_INFRASTRUCTURE
      addPackageNames(packageNames)
    }

    fun addPackageNames(additionalPackageNames: Collection<String>) {
      packageNames.addAll(additionalPackageNames)
      constructorArgumentValues.addIndexedArgumentValue(0, StringUtils.toStringArray(this.packageNames))
    }
  }

}
