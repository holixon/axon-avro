package bankaccount.conversions

import org.apache.avro.Conversion
import org.apache.avro.LogicalType
import org.apache.avro.LogicalTypes
import org.apache.avro.Schema
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Abstract conversion to be subclassed by the conversion for logical types.
 * It implements the required method for a non-parameterized logical types.
 * The subclass should implement the corresponding to- and from- method pair to construct
 * the JVM representation from Avro and back.
 */
abstract class AbstractConversion<T : Any, AVRO_TYPE : Any>(
  private val targetClass: KClass<T>,
  logicalTypeClass: KClass<out AbstractAvroLogicalTypeBase<T, AVRO_TYPE>>
) : Conversion<T>() {

  private val logicalTypeInstance = logicalTypeClass.createInstance()
  private val logicalTypeFactory by lazy {
    requireNotNull(LogicalTypes.getCustomRegisteredTypes()[logicalTypeInstance.typeName]) { "Cold not find custom logical type $logicalTypeInstance.typeName. Did you register it?" } as AbstractAvroLogicalTypeBase<T, AVRO_TYPE>
  }

  override fun getConvertedType(): Class<T> {
    return targetClass.java
  }

  override fun getLogicalTypeName(): String {
    return logicalTypeFactory.typeName
  }

  override fun getRecommendedSchema(): Schema {
    return Schema
      .create(logicalTypeFactory.schemaType)
      .apply {
        logicalTypeFactory.logicalType.addToSchema(this)
      }
  }

  /**
   * Delegates the invocation to the corresponding one based on schema type that must be implemented in the subtype.
   */
  open fun fromAvro(value: AVRO_TYPE, schema: Schema, type: LogicalType): T {
    require(logicalTypeInstance.logicalType.name == type.name) {
      "The logical type provided on conversion '${type.name}' doesn't match expected logical type '${logicalTypeInstance.logicalType.name}'."
    }
    return this.invokeConversionMethodFromAvro(value, schema, type)
  }

  /**
   * Delegates the invocation to the corresponding one based on schema type that must be implemented in the subtype.
   */
  open fun toAvro(value: T, schema: Schema, type: LogicalType): AVRO_TYPE {
    require(logicalTypeInstance.logicalType.name == type.name) {
      "The logical type provided on conversion '${type.name}' doesn't match expected logical type '${logicalTypeInstance.logicalType.name}'."
    }
    return this.invokeConversionMethodToAvro(value, schema, logicalTypeInstance)
  }

}
