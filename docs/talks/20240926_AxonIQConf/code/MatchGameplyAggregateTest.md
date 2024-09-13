```kotlin []

@Test
fun `should get result after a goal is scored and start new matchset`() {
    GIVEN
        .event(
            MatchCreatedEvent(
                roomId = roomId,
                matchId = matchId,
                matchRules = holisticRules,
                tableName = TableName("HeroMaker")
            )
        )
        .AND.event(
            DeviceSetEvent(
                roomId = roomId,
                matchId = matchId,
                deviceInfo = DeviceInfo(DevicePosition.SINGLE, deviceId),
                tableName = TableName("HeroMaker")
            )
        )
        .AND.event(
            ParticipantsChangedEvent(
                roomId = roomId, matchId = matchId, participants = MatchTeams(
                    teams = mapOf(
                        COLOR1 to MatchTeam.SoloTeam(piggy),
                        COLOR2 to MatchTeam.SoloTeam(gonzo)
                    )
                )
            )
        )
        .AND.event(
            MatchCouldStartEvent(roomId = roomId, matchId = matchId)
        )
        .AND.event(
            MatchStartedEvent(roomId = roomId, matchId = matchId, startTime = now, tableName = TableName("HeroMaker"))

        ).AND.event(
            MatchSetStartedEvent(roomId = roomId, matchId = matchId, startTime = now, matchSetNumber = 1)
        )

    WHEN
        .command(
            ScoreGoalCommand(
                roomId = roomId,
                matchId = matchId,
                playerName = piggy,
                manikinPosition = ManikinPosition.CENTER_OFFENSE,
                timestamp = now
            )
        )

    THEN
        .expectEvents(
            GoalScoredEvent(
                roomId = roomId,
                matchId = matchId,
                positionOnTable = COLOR1,
                player = piggy,
                manikinPosition = ManikinPosition.CENTER_OFFENSE,
                timestamp = now
            ),
            MatchSetScoreSetEvent(
                roomId = roomId,
                matchId = matchId,
                teamColor1 = MatchTeam.SoloTeam(piggy),
                teamColor2 = MatchTeam.SoloTeam(gonzo),
                matchSetNumber = 1,
                goalsColor1 = 1,
                goalsColor2 = 0,
                undoPossible = true
            ),
        )
}
```
