<!-- slide template="[[tpl-intermediate]]" bg="[[bg-ranked.png]]"-->

::: title
Patterns
:::

--
<!-- slide template="[[tpl-col-1-center]]" bg="[[saga-with-backing-aggregate.png]]" -->
::: title
Saga with backing-aggregate
:::

<grid drop="25 0" drag="60 50" align="topleft">

### Why?

- Email verification on account creation or change of address
- Stateful (external) protocol handling

### Implementation
* Use event handlers to send (outbound)
* Use command handlers to receive (inbound)
* Inbound might fail => use Aggregate
* Emit events, driving Saga

</grid>

--
<!-- slide template="[[tpl-col-1-2]]" -->
::: title
Set-based validation
:::

::: left

### Why?

* Email address must be unique
* Controlling exactly-once for business identity (opposed to technical identity)

### Implementation

* Command handler
* AOP style (interceptor + subscribing event handler)

:::

::: right

![[sequence-setbasedvalidation.png|1200]]

:::

<grid drop="center" drag="60 40" bg="#CDE498">

### Better way (Kudos, Yvonne!)

* EmailValidationAggregate / Actor
* `CreationPolicy.CREATE_IF_MISSING`
* create account from validation aggregate

<small>[developer.axoniq.io/w/set-based-consistency-validation-revisited](https://developer.axoniq.io/w/set-based-consistency-validation-revisited)</small>

</grid> <!-- element class="fragment" -->

--

<!-- slide template="[[tpl-col-1-center]]" data-visibility="hidden"-->
::: title
Event Sourced Configuration
:::

* Match rules is a configuration for the game play state machine
* Once used, those can't be changed anymore
* Configuration MUST be event sourced


<grid drop="25 10" style="color:red;font-size:400pt">
<bold>&#10060;</bold>
</grid><!-- element class="fragment" -->


--
<!-- slide template="[[tpl-col-1-2]]" -->
::: title
Milestone Events
:::

::: left

### Why?

* Data supply for statistics
* No commands
* Domain Event -> Milestone Event bridge

### Implementation

* Every change: Projection
* Aggregate changes: Saga
:::

::: right

![[sequence-milestoneevent.png|1000]]

:::

--
<!-- slide template="[[tpl-col-1-2]]" -->
::: title
Scatter/Gather Queries
:::

::: left

### Why?

* Distributed metrics/statistics
* Discovery / data collection
* Designed for incremental development

### Implementation

* QueryGateway

:::

::: right
![[sequence-scatter-gather.png|1000]]
:::

--
<!-- slide template="[[tpl-col-1-2]]" -->
::: title
Be ... truly reactive
:::

::: left

### Why?

* no polling
* no blocking

### Implementation

* subscribe to changes
* event-driven backend-frontend communication
* use Axon Subscription Queries

:::

::: right
![[reactive-manifesto.png]]

<small><small>[reactivemanifesto.org](https://www.reactivemanifesto.org/)</small></small>
:::
