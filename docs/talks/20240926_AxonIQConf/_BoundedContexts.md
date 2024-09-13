<!-- slide template="[[tpl-intermediate]]" bg="[[bg-ranked.png]]"-->

::: title
Bounded Contexts
:::

--

<!-- slide template="[[tpl-col-1-center]]" -->
::: title
Bounded Contexts
:::

![[domain-map.png]]

--
<!-- slide template="[[tpl-col-2-1]]" -->
::: title
Account
:::

::: left
### Observations
- Sync with external authentication
- Fraud prevention

### Implementation

- Integration with Firebase Authentication
- Allows account-based statistics
- **Sagas for email confirmation**
- **Set-based validation of the email address**

Notes:

* do not rank when it smells like fraud

:::

::: right
![[app-account.svg]]
:::

--
<!-- slide template="[[tpl-col-2-1]]" -->
::: title
Statistics
:::

::: left

### Observations

- Exhaustive amount of metrics and rankings
- Mostly time-based queries
- Different query contexts (account, team, room, ...)
- **No commands**
- **Consumes milestone events**

### Implementation

- **Scatter-gather queries for discovery**
- **Designed for incremental development**
- Integration with Influx DB
- (Can be) optimized for output
  :::

::: right
![[app-metrics.svg]]
:::

--

<!-- slide template="[[tpl-col-2-1]]" -->
::: title
Gameplay
:::

::: left

### Observations

- **Rules influence the gameplay** (configuration)
- Complex gameplay state machine

### Implementation

- Pure Kotlin domain types
- Functional core / imperative shell
- Two aggregates: room and match
- **Sagas for rematch, abandoned match**
- **Projections use SSE to push updates**

:::

::: right
![[soccer-table.svg|400]]
:::

--
<!-- slide template="[[tpl-col-1-center]]" -->
::: title
Gameplay
:::

![[ConceptBoard - Gameplay.png]]
