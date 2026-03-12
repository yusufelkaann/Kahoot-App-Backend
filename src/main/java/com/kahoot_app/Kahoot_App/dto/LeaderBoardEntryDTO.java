package com.kahoot_app.Kahoot_App.dto;

public record LeaderBoardEntryDTO(
    Long playerId,
    String nickname,
    int score,
    int rank
) {
    
}
