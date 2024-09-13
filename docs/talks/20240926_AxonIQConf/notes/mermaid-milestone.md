```mermaid
sequenceDiagram
  create participant MatchAggregate
  create participant MatchFactSummarySaga
  MatchAggregate->>MatchFactSummarySaga: MatchCreated
  MatchAggregate-->>MatchFactSummarySaga: MatchStarted
  MatchAggregate-->>MatchFactSummarySaga: GoalScored
  MatchAggregate-->>MatchFactSummarySaga: GoalScored
  MatchAggregate-->>MatchFactSummarySaga: MatchFinished
  MatchAggregate-->>MatchFactSummarySaga: MatchAccepted
  MatchFactSummarySaga-->>StatisticsProjection: MatchFactSummaryEvent (rich)
```
