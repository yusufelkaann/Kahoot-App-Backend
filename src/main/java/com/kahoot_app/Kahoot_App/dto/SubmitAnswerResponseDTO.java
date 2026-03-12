package com.kahoot_app.Kahoot_App.dto;

public record SubmitAnswerResponseDTO(
    String message,
    Long playerId,
    Integer currentScore
) {}
