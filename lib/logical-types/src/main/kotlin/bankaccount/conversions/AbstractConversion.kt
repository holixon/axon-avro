package bankaccount.conversions

import org.apache.avro.Conversion
import org.apache.avro.LogicalTypes
import org.apache.avro.Schema
import kotlin.reflect.KClass

abstract class AbstractConversion<T : Any, AVRO4K_TYPE : Any>(
  private val targetClass: KClass<T>,
  private val logicalTypeName: String,
  logicalTypeClass: KClass<out AbstractAvroLogicalTypeBase<T, AVRO4K_TYPE>>
) : Conversion<T>() {

  private val logicalTypeInstance by lazy {
    logicalTypeClass.constructors.first().call()
  }

  private val registeredInstance by lazy {
    requireNotNull(LogicalTypes.getCustomRegisteredTypes()[logicalTypeName]) { "Cold not find custom logical type $logicalTypeName. Did you register it?" } as AbstractAvroLogicalTypeBase<T, AVRO4K_TYPE>
  }


  override fun getConvertedType(): Class<T> {
    return targetClass.java
  }

  override fun getLogicalTypeName(): String {
    return registeredInstance.typeName
  }

  override fun getRecommendedSchema(): Schema {
    return Schema
      .create(registeredInstance.schemaType)
      .apply {
        registeredInstance.logicalType.addToSchema(this)
      }
  }

  fun toJvm(value: AVRO4K_TYPE): T {
    return logicalTypeInstance.toJvm(value)
  }

  fun toAvro4K(value: T): AVRO4K_TYPE {
    return logicalTypeInstance.toAvro(value)
  }
}
