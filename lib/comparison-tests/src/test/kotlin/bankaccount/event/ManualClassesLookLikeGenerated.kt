package bankaccount.event

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney
import io.toolisticon.kotlin.avro.serialization.AvroKotlinSerialization
import org.assertj.core.api.Assertions.assertThat
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test


class ManualClassesLookLikeGenerated {

  @Test
  fun getFingerPrint() {
    val event = MoneyWithdrawn("account-4711", Money.of(100, "EUR"))

    val bytes = AvroKotlinSerialization().encodeSingleObject(
      event
    )
    assertThat(bytes.fingerprint.value).isEqualTo(8652820516454024790L)
  }

  fun bankAccountCreated(cmd: CreateBankAccount) {
    BankAccountCreated
      .newBuilder()
      .setAccountId(cmd.accountId)
      .setInitialBalance(cmd.initialBalance)
      .build()
  }

  fun moneyDeposited(cmd: DepositMoney) {
    MoneyDeposited
      .newBuilder()
      .setAccountId(cmd.accountId)
      .setAmount(cmd.amount)
      .build()
  }

  fun moneyWithdrawn(cmd: WithdrawMoney) {
    MoneyWithdrawn
      .newBuilder()
      .setAccountId(cmd.accountId)
      .setAmount(cmd.amount)
      .build()
  }
}
