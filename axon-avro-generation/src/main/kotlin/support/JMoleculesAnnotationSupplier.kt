package io.holixon.axon.avro.generation.support

import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import io.holixon.axon.avro.generation.support.JMoleculesAnnotationSupplier.JMoleculesFeature.*
import io.toolisticon.kotlin.avro.value.CanonicalName
import io.toolisticon.kotlin.avro.value.CanonicalName.Companion.parse
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.buildAnnotation
import io.toolisticon.kotlin.generation.KotlinCodeGeneration.className
import io.toolisticon.kotlin.generation.builder.KotlinAnnotatableBuilder
import io.toolisticon.kotlin.generation.spec.KotlinAnnotationSpec
import io.toolisticon.kotlin.generation.spec.KotlinAnnotationSpecSupplier
import kotlin.reflect.KClass

@OptIn(ExperimentalKotlinPoetApi::class)
sealed interface JMoleculesAnnotationSupplier : KotlinAnnotationSpecSupplier {
  val feature: JMoleculesFeature

  enum class JMoleculesFeature(val markerClass: CanonicalName) {

    DDD(parse("org.jmolecules.ddd.annotation.AggregateRoot")),
    CQRS(parse("org.jmolecules.architecture.cqrs.Command")),
    EVENTS(parse("org.jmolecules.event.annotation.DomainEvent")),
    ;

    val enabled: Boolean = try {
      Class.forName(markerClass.fqn)
      true
    } catch (e: ClassNotFoundException) {
      false
    }

    fun className(type: String) = className(markerClass.namespace.value, type)
  }


  enum class PublisherType {

    /**
     * Expresses that the events published are intended for internal usage.
     */
    INTERNAL,

    /**
     * Expresses that the events published are intended for external usage.
     */
    EXTERNAL,

    /**
     * Expresses that the target audience of the events is undefined (default).
     */
    UNDEFINED;
  }


  fun addIfEnabled(builder: KotlinAnnotatableBuilder<*>) = if (feature.enabled) {
    val annotation = spec()
    builder.addAnnotation(annotation)
  } else {
    builder
  }

  fun className(type: String) = feature.className(type)

  /**
   * A domain event is a full-fledged part of the domain model, a representation of something that happened in the domain.
   *
   * Retention: RUNTIME
   * Target: Type
   */
  data class DomainEventAnnotation(
    private val eventName: CanonicalName,
  ) : JMoleculesAnnotationSupplier {

    override val feature: JMoleculesFeature = EVENTS

    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("DomainEvent")) {
      addStringMember("namespace", eventName.namespace.value)
      addStringMember("name", eventName.name.value)
    }
  }

  /**
   * Identifies a command in the context of CQRS, i.e. a request to the system for the change of data.
   *
   * Retention: RUNTIME
   * Target: TYPE
   */
  data class CommandAnnotation(
    private val commandName: CanonicalName,
  ) : JMoleculesAnnotationSupplier {
    override val feature = CQRS
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Command")) {
      addStringMember("namespace", commandName.namespace.value)
      addStringMember("name", commandName.name.value)
    }
  }

  /**
   * Identifies a domain event handler, i.e. logic to process a {@link DomainEvent}.
   *
   * Retention: RUNTIME
   * Target: METHOD, ANNOTATION_TYPE
   */
  data class DomainEventHandlerAnnotation(
    private val eventName: CanonicalName,
  ) : JMoleculesAnnotationSupplier {

    override val feature = EVENTS

    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("DomainEventHandler")) {
      addStringMember("namespace", eventName.namespace.value)
      addStringMember("name", eventName.name.value)
    }
  }

  /**
   * Identifies a domain event publisher, i.e. logic to publish a {@link DomainEvent}.
   *
   * Retention: RUNTIME
   * Target: METHOD, ANNOTATION_TYPE
   */
  data class DomainEventPublisherAnnotation(
    private val eventName: CanonicalName,
    private val publisher: PublisherType = PublisherType.UNDEFINED,
  ) : JMoleculesAnnotationSupplier {
    override val feature = EVENTS

    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("DomainEventPublisher")) {
      addStringMember("publishes", eventName.fqn)
      addEnumMember("type", publisher)
    }
  }

  /**
   * Annotation to marks domain events as to be externalized, which means that they are intended to be published to
   * infrastructure outside the application.
   *
   * Retention: RUNTIME
   * Target: TYPE
   */
  data class ExternalizedAnnotation(
    private val target: String,
  ) : JMoleculesAnnotationSupplier {

    override val feature = EVENTS

    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Externalized")) {
      addStringMember("target", target)
    }
  }

  /**
   * Identifies a command dispatcher in the context of CQRS, i.e. logic to dispatch a {@link Command}.
   *
   * Retention: RUNTIME
   * Target: METHOD, ANNOTATION_TYPE
   */
  data class CommandDispatcherAnnotation(
    private val commandName: CanonicalName,
  ) : JMoleculesAnnotationSupplier {

    override val feature = CQRS

    override fun spec(): KotlinAnnotationSpec = buildAnnotation(feature.className("CommandDispatcher")) {
      /**
       * Optional identification of the command dispatched by this dispatcher.
       */
      addStringMember("dispatches", commandName.fqn)
    }
  }

  /**
   * Identifies a command handler in the context of CQRS, i.e. logic to process a {@link Command}.
   *
   * Retention: RUNTIME
   * Target: METHOD, ANNOTATION_TYPE, CONSTRUCTOR
   */
  data class CommandHandlerAnnotation(
    private val commandName: CanonicalName,
  ) : JMoleculesAnnotationSupplier {
    override val feature = CQRS
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("CommandHandler")) {
      addStringMember("namespace", commandName.namespace.value)
      addStringMember("name", commandName.name.value)
    }
  }

  /**
   * Identifies a query model element in the context of CQRS.
   *
   * Retention: RUNTIME
   *   @Target(ElementType.TYPE)
   */
  data object QueryModelAnnotation : JMoleculesAnnotationSupplier {
    override val feature = CQRS
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("QueryModel"))
  }

  /**
   * Identifies an aggregate root, i.e. the root entity of an aggregate.
   *
   * Retention: RUNTIME
   * Target: CLASS
   */
  data object AggregateRootAnnotation : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("AggregateRoot"))
  }

  /**
   * An association to an [org.jmolecules.ddd.annotation.AggregateRoot].
   *
   * Retention: RUNTIME
   * Target: ANNOTATION_CLASS, FIELD, PROPERTY, PROPERTY_GETTER
   */
  data class AssociationAnnotation(
    private val aggregateType: KClass<*>? = null,
  ) : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Association")) {
      aggregateType?.let {
        this.addKClassMember("aggregateType", it)
      }
    }
  }

  /**
   * Identifies an [Entity].
   *
   * Retention: RUNTIME
   * Target: CLASS
   */
  data object EntityAnnotation : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Entity"))
  }

  /**
   * Identifies a [Factory].
   *
   * Retention: RUNTIME
   * Target: CLASS
   */
  data object FactoryAnnotation : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Factory"))
  }

  /**
   * Declares a field (or a getter) of a class to constitute the identity of the corresponding class.
   *
   * Retention: RUNTIME
   * Target: ANNOTATION_CLASS, FIELD, PROPERTY, PROPERTY_GETTER
   */
  data object IdentityAnnotation : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Identity"))
  }


  /**
   * Identifies a [Repository].
   *
   * Retention: RUNTIME
   * Target: CLASS
   */
  data object RepositoryAnnotation : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Repository"))
  }

  /**
   * Identifies a domain [Service].
   *
   * Retention: RUNTIME
   * Target: CLASS
   */
  data object ServiceAnnotation : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("Service"))
  }

  /**
   * Identifies a value object. Domain concepts that are modeled as value objects have no conceptual identity or
   * lifecycle. Implementations should be immutable, operations on it are side effect free.
   *
   * Retention: RUNTIME
   * Target: CLASS
   */
  data object ValueObjectAnnotation : JMoleculesAnnotationSupplier {
    override val feature = DDD
    override fun spec(): KotlinAnnotationSpec = buildAnnotation(className("ValueObject"))
  }
}
