If your application is using Event Sourcing as persistence strategy, the total disk space usage of your event store might be an interesting 
parameter. Assuming that you are using some event store technology (Axon Server or any other Event Store of your choice), this value depends
on the serialization format. Currently, Axon Framework supports the following serialization formats: Java Binary, JSON via Jackson, 
CBOR via Jackson and XML via XStream. If you are planing to use this extension, you are extending this list by Avro Single Object Encoded.

In this section, we publish results we conducted during comparison of the formats above. Since Java Binary serialization should not be used
in any productive systems due to security considerations, we excluded it from the test and conducted comparison on XML, JSON, CBOR and Avro.

## Test setup

- Created a small banking application, allowing to create account and deposit and withdraw money. It consists of three commands, one aggregate,
  three events and a projection.
- The application has an API to batch creation of accounts and executing operations on them
- For every account, we executed money transfer operations in pairs (withdraws, disposals)
- Results in 2 * M + 1 events per account
- Running with N=10 accounts, M=100 operation pairs, resulting in 10 * 201 = 2010 events per run
- The total test was to execute 5 runs resulting in 10050 events in the Axon Server
- For Axon Server we used a docker-version version `axoniq/axonserver:2023.2.6-jdk-17` with small event segment files 977kb (1000000 bytes)
- The test application and Axon Server were running on the same machine
- Measured time for each run (The test was executed on Ubuntu 22.04.4 LTS running on a Lenovo P1 with Intel® Core™ i7-8850H CPU @ 2.60GHz × 12, 32,0 GiB, SSD M.2 PCIe NVMe)
- Check file system and calculate storage requirements

## Test results

### XStream

#### Timings

1. Time: 23221ms
2. Time: 25597ms
3. Time: 24300ms
4. Time: 22654ms
5. Time: 21925ms

Resulting in an average of 23539ms.

#### Disk space usage

```
-rw-r--r-- 1 root root 1000000 Jul 11 22:27 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 11 22:29 00000000000000002409.events
-rw-r--r-- 1 root root 1000000 Jul 11 22:30 00000000000000004818.events
-rw-r--r-- 1 root root 1000000 Jul 11 22:31 00000000000000007227.events
-rw-r--r-- 1 root root 1000000 Jul 11 22:32 00000000000000009636.events
```

#### Representation in the event store

BankAccountCreated:
```xml
<bankaccount.event.BankAccountCreated>
  <accountId>55eeba20-cdae-40fd-b418-b0f7b38cb676</accountId>
  <initialBalance>100</initialBalance>
</bankaccount.event.BankAccountCreated>
```

MoneyDeposited
```xml
<bankaccount.event.MoneyDeposited>
  <accountId>55eeba20-cdae-40fd-b418-b0f7b38cb676</accountId>
  <amount>11</amount>
</bankaccount.event.MoneyDeposited>
```

MoneyWithdrawn
```xml
<bankaccount.event.MoneyWithdrawn>
  <accountId>55eeba20-cdae-40fd-b418-b0f7b38cb676</accountId>
  <amount>3</amount>
</bankaccount.event.MoneyWithdrawn>
```
### Jackson JSON

#### Timings

1. Time: 24300ms
2. Time: 18681ms
3. Time: 17774ms
4. Time: 16740ms
5. Time: 17603ms

Resulting in an average of 19019ms.

#### Disk usage

```
-rw-r--r-- 1 root root 1000000 Jul 11 22:42 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 11 22:44 00000000000000003096.events
-rw-r--r-- 1 root root 1000000 Jul 11 22:45 00000000000000006192.events
-rw-r--r-- 1 root root 1000000 Jul 11 22:45 00000000000000009288.events

```

BankAccountCreated:
```json
{"accountId":"4633c9ec-d244-41b6-9721-653234cc876d","initialBalance":100}
```
MoneyDeposited:
```json
{"accountId":"4633c9ec-d244-41b6-9721-653234cc876d","amount":16}
```
MoneyWithdrawn:
```json
{"accountId":"4633c9ec-d244-41b6-9721-653234cc876d","amount":5}
```

## Jackson CBOR

#### Timings

1. Time: 17347ms
2. Time: 16690ms
3. Time: 16163ms
4. Time: 16364ms
5. Time: 16674ms

Resulting in an average of 16647ms.

#### Disk usage

```
-rw-r--r-- 1 root root 1000000 Jul 11 23:28 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 11 23:29 00000000000000003150.events
-rw-r--r-- 1 root root 1000000 Jul 11 23:30 00000000000000006300.events
-rw-r--r-- 1 root root 1000000 Jul 11 23:30 00000000000000009450.events

```

BankAccountCreated
```
�iaccountIdx$c915f303-8e46-48c1-b7be-4d1ab9b6c304ninitialBalanced�
```

MoneyDeposited
```
�iaccountIdx$c915f303-8e46-48c1-b7be-4d1ab9b6c304famount�
```

MoneyWithdrawn
```
�iaccountIdx$c915f303-8e46-48c1-b7be-4d1ab9b6c304famount�
```

### Avro with Java generated classes

#### Timings

1. Time:
2. Time:
3. Time:
4. Time:
5. Time:

Resulting in an average of ms.

#### Disk usage

```

```

### Avro with KotlinX serialized classes

#### Timings

1. Time:
2. Time:
3. Time:
4. Time:
5. Time:

Resulting in an average of ms.

#### Disk usage

```

```


## How to compare / anatomy of the test application

In order to make it comparable we had to use two slightly different applications. The XML, JSON and CBOR applications were using Kotlin 
Data classes for events. Those have been serialized by the corresponding Axon Serializer and sent to Axon Server. For the Avro stack we
conducted two tests: one using KotlinX Avro Serialization and another using classes created by Avro Java generator. The latter generator 
has a particular form of how classes look like (static builder, internal fields not reflecting the attributes) and therefor we created two 
versions of event classes.

If you look into `lib/` folder of the library you will find the following modules:

- `fixture-events-manual`: hand-written Kotlin Data classes with KotlinX `Serializable` annotation, having the same signature as generated by Apache Avro Java generator
- `fixture-events-avro-java`: Avro specifications, used for the generation of Java classes by Apache Avro Java Maven plugin
- `fixture-application`: banking application classes (aggregate, commands, projection)
- `comparison-tests`: comparison tests

## Run tests on your own

Every start of application will produce one run (2010 events). In our tests we re-started the application five times. 

- XStream: start `bank.ComparisonApplication` with Spring profile `xstream`
- Jackson JSON: start `bank.ComparisonApplication` with Spring profile `jackson`
- Jackson CBOR: start `bank.ComparisonApplication` with Spring profile `cbor`
- Avro KotlinX Serialization: start `bank.ComparisonApplication` with Spring profile `avro`
- Avro Java generated classes: activate Maven profile `avro-java`, rebuild the application and start `bank.ComparisonApplication` with Spring profile `avro`  
