<!-- slide template="[[tpl-intermediate]]" bg="[[holisticon-bg.svg]]" -->

::: title
Serialization - What we want?
:::

--

<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
Schema first
:::

::: left

### <i class="fas fa-question-circle fa-1x"></i> Problem

- Business Events
- You want schema based messages (especially events)
- _you do not want to rely on first-class-citizens in your systems to be done right by some developer hacking in one IDE_
- Long-lived - understand what's in an event years from now

:::


::: right

### Solution <i class="fas fa-exclamation-circle fa-1x"></i>

- Schema based
- directly expressed with business using event storming/modelling
- schema hash encoded in message - able to look-up building plan of message from registry

:::


--

<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
Schema Evolution
:::

::: left

### <i class="fas fa-question-circle fa-1x"></i> Problem

- Schema Evolution/Upcasting
- not from one class to another class but from one schema to another schema
- mental model mismatch

:::


::: right

### Solution <i class="fas fa-exclamation-circle fa-1x"></i>

- Upcasting using GenericRecord as intermediate
- example code snippet
- Source, Intermediate and Target are schema based

:::


--

<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
Ubiquitous Language
:::

::: left

### <i class="fas fa-question-circle fa-1x"></i> Problem

- DDD: Express Ubiquitous Language in code
- Your domain is about amounts and customer names ...
+ ... not about ints and strings

:::

::: right

### Solution <i class="fas fa-exclamation-circle fa-1x"></i>

- logical type
  - uuid
  - money
  - customerId

- example snippet

:::

--

<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
Distribution
:::

::: left

### <i class="fas fa-question-circle fa-1x"></i> Problem

- All systems need to understand the domain messages
- versioned jars with byte code and upcasters
- serialization configuration

:::


::: right

### Solution <i class="fas fa-exclamation-circle fa-1x"></i>

- Distribute model description via registry/web/...
- Code generation on consumer side
- Distinguish reader/writer schema compatibility modes.

:::

--
<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon.svg]]" -->

::: title
Don't repeat yourself
:::

::: left

### <i class="fas fa-question-circle fa-1x"></i> Problem

* Do not spent time writing glue code and skeletons that you can derive from schema declaration

:::


::: right

### Solution <i class="fas fa-exclamation-circle fa-1x"></i>

Use code generator for:

* Protocol + messages to define system/context behavior
* Express relations between Query/Result, Command/Event/Exception
* Contract (interfaces) for provider and consumer implementers
* Code snippets holi bank

:::


--

<!-- slide template="[[tpl-col-1-center]]" bg="[[holisticon-bg.svg]]" -->

::: title
Does it work?
:::


Showtime video

--

<!-- slide template="[[tpl-final]]" bg="[[holisticon-bg.svg]]" data-background-opacity="0.2" -->

::: title

Image of stats

:::

::: left

### Requirements

* needs to be fast
* needs to be compact

:::

::: right

### Results

* min 10% less storage space
* as fast as jackson (~ +/-)

:::
