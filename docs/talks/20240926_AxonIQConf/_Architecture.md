<!-- slide template="[[tpl-intermediate]]" bg="[[bg-ranked.png]]"-->

::: title
Architecture
:::

--
<!-- slide template="[[tpl-col-1-center]]" -->

::: title
What is clean architecture?
:::

![[clean-architecture.svg|1500]]

--
<!-- slide template="[[tpl-col-1-1]]" -->

::: title
What is clean architecture?
:::

::: left
### In Port / In Adapter

- Invoke Use Cases
- One Port per Use Case

### Out Port / Out Adapter

- Adopt Technology supporting the Domain and Application (Storage, Messaging, Security)
- At least one Adapter per Technology

### Takeaways

- Domain must be technology independent
- Application should be infrastructure agnostic
:::

::: right
![[book-hands-dirty.svg]]
:::

--

<!-- slide template="[[tpl-col-1-1]]" -->

::: title
What is CQRS/ES?
:::

::: left
### CQRS

- architectural pattern, separating
- Write (command) model
- Read  (query) model(s)
- Eventually consistent

### ES (event sourcing)

- persistence strategy
- Emit events on state change
- Use events for storage

### Benefits

- Asymmetric command / query (functional / non-functional)
- Very adaptive to incremental development

:::

::: right
![[axon-cqrs.png]]
:::

--

<!-- slide template="[[tpl-col-1-2]]" -->

::: title
What is CQRS/ES?
:::

::: left

### Command Model

- Validation
- State changes using Events
- Event-sourced

### Query Model

- Projection
- Optimized for Queries

:::

::: right
![[diagram-cqrses.svg]]
:::

