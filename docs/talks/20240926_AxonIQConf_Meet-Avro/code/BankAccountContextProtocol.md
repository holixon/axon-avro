```json[]
{
  "namespace": "holi.bank",
  "protocol": "BankAccountContext",
  "doc": "Protocol declaration for bank-account.",
  "types": [
    {
      "name": "CreateBankAccountCommand",
      "doc": "Creates a new bank account with given accountId",
      "meta": {
        "revision": "1",
        "type": "command"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string",
          "meta": {
            "type": "association"
          }
        },
        {
          "name": "initialBalance",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "WithdrawMoneyCommand",
      "doc": "Withdraws money from a bank account",
      "meta": {
        "revision": "1",
        "type": "command"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string",
          "meta": {
            "type": "association"
          }
        },
        {
          "name": "amount",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "DepositMoneyCommand",
      "doc": "Deposits money from a bank account",
      "meta": {
        "revision": "1",
        "type": "command"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string",
          "meta": {
            "type": "association"
          }
        },
        {
          "name": "amount",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "IllegalInitialBalance",
      "type": "error",
      "fields": [
        {
          "name": "message",
          "type": "string"
        }
      ]
    },
    {
      "name": "BankAccountCreatedEvent",
      "doc": "A new BankAccount has been created with initial balance.",
      "meta": {
        "revision": "1",
        "type": "event"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string"
        },
        {
          "name": "initialBalance",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "MoneyDepositedEvent",
      "doc": "Money was deposited from BankAccount.",
      "meta": {
        "revision": "1",
        "type": "event"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string"
        },
        {
          "name": "amount",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "MoneyWithdrawnEvent",
      "doc": "Money was withdrawn from BankAccount.",
      "meta": {
        "revision": "1",
        "type": "event"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string"
        },
        {
          "name": "amount",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "FindCurrentBalanceByAccountIdQuery",
      "doc": "Searches bank account balance for given account id.",
      "meta": {
        "revision": "1",
        "type": "query"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string"
        }
      ]
    },
    {
      "name": "FindAllMoneyTransfersByAccountIdQuery",
      "doc": "Searches bank account money transfers for given account id.",
      "meta": {
        "revision": "1",
        "type": "query"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string"
        }
      ]
    },
    {
      "name": "CurrentBalance",
      "doc": "Current balance of bank account.",
      "meta": {
        "type": "queryResponse"
      },
      "type": "record",
      "fields": [
        {
          "name": "accountId",
          "type": "string"
        },
        {
          "name": "balance",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "MoneyTransfer",
      "doc": "Money transfer.",
      "meta": {
        "type": "queryResponse"
      },
      "type": "record",
      "fields": [
        {
          "name": "type",
          "type": {
            "name": "MoneyTransferType",
            "type": "enum",
            "symbols": [
              "DEPOSIT",
              "WITHDRAWAL"
            ]
          }
        },
        {
          "name": "balance",
          "type": "int",
          "default": 0
        }
      ]
    },
    {
      "name": "MoneyTransfers",
      "type": "record",
      "fields": [
        {
          "name": "items",
          "type": {
            "type": "array",
            "items": "MoneyTransfer",
            "default": []
          }
        }
      ]
    }
  ],

  "messages": {
    "createBankAccount": {
      "doc": "Creates a new bank account.",
      "meta": {
        "group": "BankAccountAggregate",
        "type": "deciderInit"
      },
      "request": [
        {
          "name": "command",
          "type": "CreateBankAccountCommand"
        }
      ],
      "response": "BankAccountCreatedEvent",
      "errors": [
        "IllegalInitialBalance"
      ]
    },
    "withdrawMoney": {
      "meta": {
        "group": "BankAccountAggregate",
        "type": "decider"
      },
      "request": [
        {
          "name": "command",
          "type": "WithdrawMoneyCommand"
        }
      ],
      "response": "MoneyWithdrawnEvent"
    },
    "depositMoney": {
      "meta": {
        "group": "BankAccountAggregate",
        "type": "decider"
      },
      "request": [
        {
          "name": "command",
          "type": "DepositMoneyCommand"
        }
      ],
      "response": "MoneyDepositedEvent"
    },

    "findCurrentBalanceForAccountId": {
      "meta": {
        "group": "BankAccountProjection",
        "type": "query"
      },
      "request": [
        {
          "name": "query",
          "type": "FindCurrentBalanceByAccountIdQuery"
        }
      ],
      "response": [
        "null",
        "CurrentBalance"
      ]
    },
    "findAllMoneyTransfersForAccountId": {
      "meta": {
        "group": "BankAccountProjection",
        "type": "query"
      },
      "request": [
        {
          "name": "query",
          "type": "FindAllMoneyTransfersByAccountIdQuery"
        }
      ],
      "response": "MoneyTransfers"
    }
  }
}
```
