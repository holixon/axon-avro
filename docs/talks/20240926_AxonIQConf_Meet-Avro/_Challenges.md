<!-- slide template="[[tpl-intermediate-subtitle]]" bg="[[holisticon-bg.svg]]" -->

::: title
DDD & CQRS/ES
:::

::: subtitle
The Challenges
:::

--

<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
Business Challenges
:::

::: left

### Design

+ Messages are first class citizens
+ Your system behavior and state is expressed by `commands`, `events` and `queries`
  + Messages have semantics (a `command` triggers an `event`, a `response` answers a `query`)
+ Business and IT need to communicate in terms of _messages_ and _UBL_
  + Your Business does not work with `String` and `Integer`, it uses `CustomerId` and `Amount`
+ _You do not want to rely on first-class-citizens in your systems to be done right by some developer hacking in one IDE_

:::

::: right

### Evolution

+ `Events` are stored long term in an append only store
+ You will need to identify and read `events` years from now
+ If you are forced to change an event you need to to know if it is still compatible to the previos version

:::

--

<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" -->

::: title
IT Challenges
:::

::: left

### Development

+ Your system will consist of multiple micro-services
+ You will need to share your `commands`, `events` and `queries` between services and teams.
+ The semantics of your messages and the axon framework require you to implement specific command- event- and query handlers
+ You must not make any mistakes
+ You must always use the latest models


:::

::: right

### Operation

+ `Events` are stored long term in an append only store
+ You want to have a compact storage format that does not waste precious disk space
+ If business changes events, you will have to develop smart upcasting, keeping compatibility
+ Upcasters need to be distributed as well

### QA

+ You need to test handlers, messages and upcasters

:::
