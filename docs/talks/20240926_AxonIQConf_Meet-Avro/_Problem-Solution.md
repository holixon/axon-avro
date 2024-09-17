<!-- slide template="[[tpl-intermediate]]" bg="[[]]"-->

::: title
What we want?
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
- Long-lived - understand what's in an event years from now

:::


::: right

### Solution

- Schema based
- __example code snippet__
- directly expressed with business using event storming/modelling

- schema hash encoded in message - able to look-up building plan of message from registry

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
- Your domain is about amounts and customer names, not about ints and strings

:::


::: right

### Solution

- logical type money, customerId
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

--
<!-- slide template="[[tpl-col-1-1]]" -->

::: title
DRY: Don't repeat yourself
:::

::: left

### Problem

* Do not spent time writing glue cold and skeletons that you can derive from schema declaration


:::


::: right

### Solution

Use code generator for:

* Protocol + messages to define system/context behavior
* Express relations between Query/Result, Command/Event/Exception
* Contract (interfaces) for provider and consumer implementers
* Code snippets holi bank


:::


--

<!-- slide template="[[tpl-col-1-center]]" -->

::: title
Does it work?
:::


Showtime video

--

<!-- slide template="[[tpl-final]]" bg="[[]]" data-background-opacity="0.2" -->

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

