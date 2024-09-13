
```mermaid
sequenceDiagram
	Client->>Interceptor: CreateAccount(axon@iq)
	Interceptor->>EmailRepository: exists(axon@iq)
	EmailRepository-->>Interceptor: false
	Interceptor ->> Aggregate: CreateAccount(axon@iq)
	Aggregate ->> EmailEventHandler: AccountCreated(axon@iq)
	EmailEventHandler ->> EmailRepository: save(...,axon@iq)
	EmailEventHandler -->> Aggregate: ..
	Aggregate -->> Client: ..    
```
