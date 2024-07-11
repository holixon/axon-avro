package bankaccount.event

import bankaccount.command.CreateBankAccount
import bankaccount.command.DepositMoney
import bankaccount.command.WithdrawMoney


class ManualClassesLookLikeGenerated {

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
