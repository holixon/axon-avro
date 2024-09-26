<!-- slide template="[[tpl-intermediate-subtitle]]" bg="[[holisticon-bg.svg]]" data-background-opacity=".2" -->

::: title
DDD & CQRS/ES
:::

::: subtitle
The Challenges
:::

--
<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" data-background-opacity=".2" -->

::: title
Business Challenges
:::

::: left

### Design

+ Messages are **first class citizens** _(Event Storming, Event Modeling)_
+ Your system **behavior** and state is expressed by `commands`, `events` and `queries`
  + `Messages` have **semantics** (a `command` triggers an `event`, a `response` answers a `query`)
+ Business and IT need to **communicate** in terms of `messages` and **UBL**
  + Your Business does not work with `String` and `Integer`, it uses `CustomerId` and `Amount`
:::

::: right
### Evolution

+ You will need to **identify** and read `events` years from now
  + `Events` are stored **long term** in an append-only store
+ If you are forced to change an `event` you need to know if it is still **compatible** to the previous version
+ <i class="fa fa-ellipsis-h" aria-hidden="true"></i>
+ _You do not want to rely on some developer **hacking** everything correctly in some IDE_
:::

--

<!-- slide template="[[tpl-col-1-1]]" bg="[[holisticon-bg.svg]]" data-background-opacity=".2" -->

::: title
IT Challenges
:::

::: left

### Development

+ Your system will consist of multiple **micro-services** _(polyglot?)_ 
+ You will need to **share** your `commands`, `events` and `queries` between services and teams
  + You must always use the latest **revisions**
  + You must share **serialization** details as well
+ The semantics of your messages and the **Axon Framework** require you to implement specific handler functions
+ You must not make any **mistakes**!
:::

::: right
### Operation

+ No overhead: high **performance** serialization
+ Long term storage: You want to have a **compact storage format** that does not waste precious disk space
+ If business changes events, you will have to develop smart **upcasting**, keeping compatibility
  + _`Upcasters` need to be **distributed** as well_

### QA

+ You need to **test** `handlers`, `messages` and `upcasters`
:::
