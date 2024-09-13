<!-- slide template="[[tpl-intermediate]]" bg="[[bg-ranked.png]]"-->

::: title
Solutions
:::

--
<!-- slide template="[[tpl-col-1-2]]" -->

::: title
Reactive ... with webflux
:::

::: left
* Use reactive subscriptions
* Use axon-reactor library
* Use Server Sent Events
* AVOID: single thread per connected client
* Use Spring WebFlux instead of MVC
:::

::: right
<grid drag="100 100" drop="0 0" align="topleft">
![[ServerSentEvents.md]]
</grid>
:::

--
<!-- slide template="[[tpl-col-1-center]]" -->

::: title
Testing an event sourced system
:::

![[the-great-pyramid-of-giza-pie-chart.png|1400]]

--
<!-- slide template="[[tpl-col-1-center]]" -->

::: title
axon-testing-jgiven
:::

![[MatchGameplyAggregateTest.md]]

[github.com/holixon/axon-testing](https://github.com/holixon/axon-testing)

--
<!-- slide data-background-iframe="2023-09_axoniq-conf/code/jgiven-html/index.html"  data-background-interactive -->

--
<!-- slide template="[[tpl-col-1-2]]" -->

::: title
axon-testing-upcaster
:::

::: left
* Start from event (JSON, XML, Avro)
* Construct a stream of Intermediate Representation
* Pass to Upcaster
* De-serialize
* Assert 
:::

::: right
<grid drag="100 100" drop="0 0" align="topleft">
![[AccountCreatedEventUpcastingKotlinTest]]
</grid>
:::

--
<!-- slide template="[[tpl-col-1-center]]" -->

::: title
axon-testing-upcaster
:::

``` [9-12, 19-25]
src
 └─ test
    ├── kotlin
    │   └── io
    │       └── holixon
    │           └── axon
    │               └── testing
    │                   └── examples
    │                       └── upcaster
    │                           └── junit5
    │                               └── kotlin
    │                                   └── AccountCreatedEventUpcastingKotlinTest.kt
    └── resources
        └── io
            └── holixon
                └── axon
                    └── testing
                        └── examples
                            └── upcaster
                                └── junit5
                                    └── kotlin
                                        └── AccountCreatedEventUpcastingKotlinTest
                                            ├── upcasts_account_created_jackson
                                            │   ├── 1__fixture.bankaccount.event.AccountCreatedEvent__12.json
                                            │   └── 1__fixture.bankaccount.event.AccountCreatedEvent__13__result.json
                                            └── upcasts_account_created_xstream
                                                └── 1__fixture.bankaccount.event.AccountCreatedEvent__0.xml
```

[github.com/holixon/axon-testing](https://github.com/holixon/axon-testing)

--
<!-- slide template="[[tpl-title-col-1-1-footer]]" -->

::: title
axon-testing-assert
:::


![[Assert.md]]

::: left
* Nobody likes Hamcrest
* We rely on AssertJ
* Type-specific assertions for axon tests
:::

::: right
* Currently:
	* Asserts on Intermediate Representation
* Future:
	* More asserts on AxonFramework artifacts
:::
