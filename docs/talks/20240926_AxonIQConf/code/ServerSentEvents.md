```kotlin []
class GameplayDomainService(private val reactorQueryGateway: ReactorQueryGateway) {  
  
  fun getMatchUpdateStream(roomId: RoomId, matchId: MatchId): Flux<MatchInfo> {  
    val queryInstance = MatchInfoQuery.FindById(roomId = roomId, id = matchId)  
    return reactorQueryGateway  
      .subscriptionQuery(queryInstance, queryInstance.queryReturnType, queryInstance.queryReturnType)  
      .flatMapMany {  
        it.initialResult()  
          .concatWith(it.updates())  
          .doOnCancel { it.cancel() }  
      }  
    }  
}
```

```kotlin[]
class MatchQueryHandler {  
  fun getMatchUpdatesStream(request: ServerRequest): Mono<ServerResponse> {  
    return gameplayDomainService  
      .getMatchUpdateStream(request.roomId(), request.matchId())  
      .toEventStream(MatchInfoDto::class.java) { it.toDto(null) }  
  }  
  
  fun <T, DTO> Flux<T>.toEventStream(sseType: Class<DTO>, toDto: (T) -> DTO): Mono<ServerResponse> =  
    ServerResponse.ok().contentType(TEXT_EVENT_STREAM).body(  
      Flux.merge(  
        this.mapNotNull { info -> ServerSentEvent.builder(toDto.invoke(info)).build() },  
        heartbeatFlux()  
      ), sseType  
    ).switchIfEmpty(ServerResponse.notFound().build())  
  
  fun <T : Any> heartbeatFlux(): Flux<ServerSentEvent<T>> = Flux  
    .interval(Duration.of(HEARTBEAT_INTERVAL, SECONDS))  
    .map { ServerSentEvent.builder<T>().comment(HEARTBEAT_TEXT).build() }  
}
```
