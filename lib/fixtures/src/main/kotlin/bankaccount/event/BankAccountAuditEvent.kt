package bankaccount.event

import bankaccount.conversions.MoneyLogicalType
import com.github.avrokotlin.avro4k.serializer.InstantSerializer
import kotlinx.serialization.Serializable
import org.javamoney.moneta.Money
import java.time.Instant

@Serializable
data class BankAccountAuditEvent(
  val sequenceNumber: Long,
  @Serializable(with = InstantSerializer::class)
  val timestamp: Instant,
  val accountId: String,
  @Serializable(with = MoneyLogicalType.Serializer::class)
  val amount: Money,
  val traceId: String,
  val correlationId: String
)
