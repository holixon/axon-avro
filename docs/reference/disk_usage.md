If your application is using Event Sourcing as persistence strategy, the total disk space usage of your event store and the serialization 
speed might be an interesting parameters. Assuming that you are using some event store technology (Axon Server or any other Event Store of your choice), 
this values depends on the serialization format. Currently, Axon Framework supports the following serialization formats: Java Binary, JSON via Jackson, 
CBOR via Jackson and XML via XStream. If you are planing to use this extension, you are extending this list by Avro Single Object Encoded.

In order to use Avro Encoding, you have to rely on either KotlinX Serialization based on avro4k or on Avro Java library using a generator creating
you classes from you Avro schema which are subclassing `SpecificRecordBase`.

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
- Checked file system and calculate storage requirements
- We intentionally used a special type (`Money`) to simulate a slightly more complex scenario than just plain events with native types supported by any format


## Test results

| Format       | Average run (ms) | Time Factor | Average Event size (bytes) | Size Factor | Notes                                    |
|--------------|------------------|-------------|----------------------------|-------------|------------------------------------------|
| XStream      | 40381            | 1.588       | 1340                       | 4.253       | No additional configuration.             |
| Jackson JSON | 25456            | 1.001       | 355                        | 1.227       | Custom Object Mapper settings for Money. |
| Jackson CBOR | 25423            | 1.000       | 344                        | 1.092       | Custom Object Mapper settings for Money. |
| Avro KotlinX | 82162            | 3.232       | 315                        | 1.000       | Custom type conversions for Money.       |
| Avro Java    | 136087           | 5.323       | 315                        | 1.000       | Custom type conversions for Money.       |


For Avro we were using Single Object Encoded format (binary format with schema reference) for both KotlinX Serialized and Java generated 
(`SpecificRecordBase`) classes. This results in the identical representation (with same disk space consumption). 

You can find the detailed information about the test runs below.

### XStream

#### Timings

1. Time: 40690ms
2. Time: 40438ms
3. Time: 40162ms
4. Time: 40310ms
5. Time: 40308ms

Resulting in an average of 40381ms.

#### Disk space usage

```
-rw-r--r-- 1 root root 1000000 Jul 12 22:46 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:47 00000000000000000746.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:47 00000000000000001492.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:48 00000000000000002238.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:48 00000000000000002984.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:48 00000000000000003730.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:49 00000000000000004476.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:49 00000000000000005222.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:50 00000000000000005968.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:50 00000000000000006714.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:50 00000000000000007460.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:51 00000000000000008206.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:51 00000000000000008952.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:51 00000000000000009698.events
```

#### Representation in the event store

Just for reference, this is how it would look like if we are not using Moneta library, but just encode amount as Integer.

```xml 
<bankaccount.event.BankAccountCreated>
  <accountId>55eeba20-cdae-40fd-b418-b0f7b38cb676</accountId>
  <initialBalance>100</initialBalance>
</bankaccount.event.BankAccountCreated>
```

BankAccountCreated:
```xml
<bankaccount.event.BankAccountCreated>
  <accountId>bda57038-0f52-46aa-ab1f-8ee4d0b81768</accountId>
  <initialBalance>
    <currency class="org.javamoney.moneta.spi.JDKCurrencyAdapter">
      <baseCurrency>EUR</baseCurrency>
      <context>
        <data>
          <entry>
            <string>provider</string>
            <string>java.util.Currency</string>
          </entry>
        </data>
      </context>
    </currency>
    <monetaryContext>
      <data>
        <entry>
          <string>amountType</string>
          <java-class>org.javamoney.moneta.Money</java-class>
        </entry>
        <entry>
          <string>precision</string>
          <int>256</int>
        </entry>
        <entry>
          <string>java.lang.Class</string>
          <java-class>org.javamoney.moneta.Money</java-class>
        </entry>
        <entry>
          <string>java.math.RoundingMode</string>
          <java.math.RoundingMode>HALF_EVEN</java.math.RoundingMode>
        </entry>
      </data>
    </monetaryContext>
    <number>100</number>
  </initialBalance>
</bankaccount.event.BankAccountCreated>
```

MoneyDeposited
```xml
<bankaccount.event.MoneyDeposited>
  <accountId>bda57038-0f52-46aa-ab1f-8ee4d0b81768</accountId>
  <amount>
    <currency class="org.javamoney.moneta.spi.JDKCurrencyAdapter">
      <baseCurrency>EUR</baseCurrency>
      <context>
        <data>
          <entry>
            <string>provider</string>
            <string>java.util.Currency</string>
          </entry>
        </data>
      </context>
    </currency>
    <monetaryContext>
      <data>
        <entry>
          <string>amountType</string>
          <java-class>org.javamoney.moneta.Money</java-class>
        </entry>
        <entry>
          <string>precision</string>
          <int>256</int>
        </entry>
        <entry>
          <string>java.lang.Class</string>
          <java-class>org.javamoney.moneta.Money</java-class>
        </entry>
        <entry>
          <string>java.math.RoundingMode</string>
          <java.math.RoundingMode>HALF_EVEN</java.math.RoundingMode>
        </entry>
      </data>
    </monetaryContext>
    <number>19</number>
  </amount>
</bankaccount.event.MoneyDeposited>
```

MoneyWithdrawn
```xml
<bankaccount.event.MoneyWithdrawn>
  <accountId>bda57038-0f52-46aa-ab1f-8ee4d0b81768</accountId>
  <amount>
    <currency class="org.javamoney.moneta.spi.JDKCurrencyAdapter">
      <baseCurrency>EUR</baseCurrency>
      <context>
        <data>
          <entry>
            <string>provider</string>
            <string>java.util.Currency</string>
          </entry>
        </data>
      </context>
    </currency>
    <monetaryContext>
      <data>
        <entry>
          <string>amountType</string>
          <java-class>org.javamoney.moneta.Money</java-class>
        </entry>
        <entry>
          <string>precision</string>
          <int>256</int>
        </entry>
        <entry>
          <string>java.lang.Class</string>
          <java-class>org.javamoney.moneta.Money</java-class>
        </entry>
        <entry>
          <string>java.math.RoundingMode</string>
          <java.math.RoundingMode>HALF_EVEN</java.math.RoundingMode>
        </entry>
      </data>
    </monetaryContext>
    <number>9</number>
  </amount>
</bankaccount.event.MoneyWithdrawn>
```
### Jackson JSON

#### Timings

1. Time: 25297ms
2. Time: 25351ms
3. Time: 25070ms
4. Time: 25425ms
5. Time: 26140ms

Resulting in an average of 25456ms.

#### Disk usage

```
-rw-r--r-- 1 root root 1000000 Jul 12 22:25 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:26 00000000000000002817.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:29 00000000000000005634.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:29 00000000000000008451.events
```

In order to be able to serialize Money type, an additional `jackson-datatype-money` library was required
and the corresponding module needed to be registered. 

BankAccountCreated:
```json
{"accountId":"702bdae0-d507-4664-8ee8-d8d7e3ff5778","initialBalance":{"amount":100.00,"currency":"EUR"}}
```
MoneyDeposited:
```json
{"accountId":"702bdae0-d507-4664-8ee8-d8d7e3ff5778","amount":{"amount":19.00,"currency":"EUR"}}
```
MoneyWithdrawn:
```json
{"accountId":"702bdae0-d507-4664-8ee8-d8d7e3ff5778","amount":{"amount":10.00,"currency":"EUR"}}
```

## Jackson CBOR

#### Timings

1. Time: 26427ms
2. Time: 25224ms
3. Time: 25002ms
4. Time: 25120ms
5. Time: 25343ms

Resulting in an average of 25423ms.

#### Disk usage

```
-rw-r--r-- 1 root root 1000000 Jul 12 22:13 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:17 00000000000000002904.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:18 00000000000000005808.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:18 00000000000000008712.events
```
In order to be able to serialize Money type, an additional `jackson-datatype-money` library was required
and the corresponding module needed to be registered.

BankAccountCreated
```
�iaccountIdx$9b4b8c26-d16f-4ce3-b5ab-62beb9b711d1ninitialBalance�famountĂ!'hcurrencycEUR��
```

MoneyDeposited
```
�iaccountIdx$9b4b8c26-d16f-4ce3-b5ab-62beb9b711d1famount�famountĂ!�hcurrencycEUR��
```

MoneyWithdrawn
```
�iaccountIdx$9b4b8c26-d16f-4ce3-b5ab-62beb9b711d1famount�famountĂ!�hcurrencycEUR��
```

### Avro with Java generated classes

#### Timings

1. Time: 135915ms
2. Time: 136152ms
3. Time: 136598ms
4. Time: 135525ms
5. Time: 136245ms

Resulting in an average of 136087ms.

#### Disk usage

```
-rw-r--r-- 1 root root 1000000 Jul 12 23:37 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 12 23:42 00000000000000003165.events
-rw-r--r-- 1 root root 1000000 Jul 12 23:46 00000000000000006330.events
-rw-r--r-- 1 root root 1000000 Jul 12 23:47 00000000000000009495.events
```

BankAccountCreated
```
�K��X{lH27cd7b01-1515-4d4f-998e-4df0e08e44a7100,00 EUR
```

MoneyDeposited
```
�mN;�Z�ӦH27cd7b01-1515-4d4f-998e-4df0e08e44a712,00 EUR
```

MoneyWithdrawn
```
�VFtDj�xH27cd7b01-1515-4d4f-998e-4df0e08e44a76,00 EUR
```


### Avro with KotlinX serialized classes

#### Timings

1. Time: 82828ms
2. Time: 81669ms
3. Time: 81324ms
4. Time: 82468ms
5. Time: 82523ms

Resulting in an average of 82162 ms.

#### Disk usage

```
-rw-r--r-- 1 root root 1000000 Jul 12 21:59 00000000000000000000.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:02 00000000000000003165.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:04 00000000000000006330.events
-rw-r--r-- 1 root root 1000000 Jul 12 22:05 00000000000000009495.events
```

BankAccountCreated
```
�K��X{lH27cd7b01-1515-4d4f-998e-4df0e08e44a7100,00 EUR
```

MoneyDeposited
```
�mN;�Z�ӦH27cd7b01-1515-4d4f-998e-4df0e08e44a712,00 EUR
```

MoneyWithdrawn
```
�VFtDj�xH27cd7b01-1515-4d4f-998e-4df0e08e44a76,00 EUR
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
