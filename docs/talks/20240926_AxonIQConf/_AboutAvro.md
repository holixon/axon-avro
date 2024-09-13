<!-- slide template="[[tpl-intermediate]]" bg="[[bg-ranked.png]]"-->

::: title
Apache AVRO
:::

--

<!-- slide template="[[tpl-col-2-1]]" -->

::: title
Apache AVRO
:::

::: left

### What is avro

- Apache Avro™ is the leading serialization format for record data, and first choice for streaming data pipelines. It offers excellent schema evolution, and has implementations for the JVM (Java, Kotlin, Scala, …), Python, C/C++/C#, PHP, Ruby, Rust, JavaScript, and even Perl.
- Platform independent - FE/BE, LLM, Messaging, ...

:::

::: right

Illustration?

:::

--

::: title
What we want
:::

--

<!-- slide template="[[tpl-col-1-1]]" -->

::: title
Schema first
:::

::: left

### Problem 

- Business Events 
- You want schema based messages (especially events)
- you do not want to rely on first-class-citizens in your systems to be done right by some developer hacking in one IDE
- Long lived - understand whats in an event years from now

:::


::: right

### Solution 

- Schema based 
- __example code snippet__ 
- directly expressed with business using event storming/modelling

- schema hash encoded in message - able to lookup building plan of message from registry

:::


--

<!-- slide template="[[tpl-col-1-1]]" -->

::: title
Schema Evolution
:::

::: left

### Problem 

- Schema Evolution/Upcasting 
- not from one class to another class but from one schema to another schema
- mental model mismatch

:::


::: right

### Solution 

- Upcasting using GenericRecord as intermediate 
- example code snippet
- Source, Intermediate and Target are schema based

:::


--

<!-- slide template="[[tpl-col-1-1]]" -->

::: title
Ubiquitous Language
:::

::: left

### Problem 

- Express Ubiquitous Language in code 
- Do not use ints and strings to define your domain

:::


::: right

### Solution 

- logical type money, customerid 
- example snippet

:::

--

<!-- slide template="[[tpl-col-1-1]]" -->

::: title
Distribution
:::

::: left

### Problem 

- All systems need to understand the domain messages
- versioned jars with byte code and upcasters
- serialization configuration

:::


::: right

### Solution 

- Distribute model description via registry/web/... 
- Code generation on consumer side
- Distinguish reader/writer schema compatibility modes.

:::

