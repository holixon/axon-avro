<!-- slide template="[[tpl-intermediate-subtitle]]" bg="[[holisticon-bg.svg]]" -->

::: title
DDD & CQRS/ES
:::

::: subtitle
The Solution
:::

--
<!-- slide template="[[tpl-intermediate-subtitle]]" bg="[[holisticon-bg.svg]]" -->

::: title
Apache `AVRO`
:::

::: subtitle
plus our `axon-avro` extension
:::

--
<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
What is Apache `AVRO`?
:::

::: left

### Self Promotion

> Apache Avro™ is the leading serialization format for record data, and first choice for
> streaming data pipelines.
>
>
> It offers excellent **schema evolution**, and has implementations for the JVM (Java, Kotlin,
> Scala, …), Python, C/C++/C#, PHP, Ruby, Rust, JavaScript, and **even** Perl.

:::

::: right

### Fact sheet

* Created in 2011, as part of Hadoop ecosystem
* Platform independent - FE/BE, LLM, Messaging, ...
* Schema-First approach for types and protocols
  * Defines a type system _(int, string, long, array, record, ...)_
  * Defines serialization encodings  _(JSON, Binary, Single Object Encoding)_
::: 

--
<!-- slide template="[[tpl-col-1-center-wide]]" bg="[[holisticon-bg.svg]]" -->

::: title
What is Apache `AVRO`?
:::

Example of a simplified bank-account context (json):

![[BankAccountContextProtocol]]

--
<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
How can Apache `AVRO` help you?
:::

::: left

### Design

+ **Schema-First** Approach
  + Directly express your `commands`, `events` and `queries` via `schema` declaration
+ Describe context behavior as **Protocol**s  
  + simple `messages` abstraction for `request`, `response` and `error`
+ centralized **Schema-Registry**
+ UBL supported by **Logical Types**
  + define custom value types (`CustomerId`, ...)
+ Extensible `schema` declaration
  + Express (non)technical properties inside the schema 

:::

::: right
### Evolution

+ Schema **compatibility** matrix
  + different levels of forward/backward compatibility
  + separation of `writer` and `reader` **schema**
+ Schema **fingerprint** embedded in messages
  + ability to look up the `writer-schema`
+ Simplified **upcasting**
  + definition of **default** values for new properties
  + **Conflicts** can be determined on `schema` level
  + The **intermediate representation** _(`GenericRecord`)_ understands the underlying `schema` (no mental model mismatch)
:::

--
<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
How can our `AXON-AVRO` help?
:::

::: left
### Development

+ Do not share versioned **libraries** between teams, use **schema registry**
<!--  + centralized `schema` declarations, always access the latest revision -->
+ Generate **Core API**
  + (data) classes for `commands`, `events`, `queries`, `responses` and `errors` 
+ Generate **Command Model**
  + `interfaces` for `CommandHandler` and `EventSourcingHandler` functions
  + custom `CommandGateway` for type safe publishing
+ Generate **Query Model**
  + `interfaces` for `QueryHandler` functions
  + `QueryGateway` extensions for type safe and reliant `query`/`response` clients
:::

::: right
### Operation

+ Pluggable `AVRO` serializer
  + auto configurable 
  + about as fast as `jackson`
+ **Compact storage** via `Single Object Encoded` bytes 
  + reduce disk usage by min 10% < `jackson cbor`
+ Axon dashboard **plugin** to read events as `json`

### QA

+ Avoid making any **mistakes** because boilerplate is derived from `schema`!
:::
