package com.kahoot_app.Kahoot_App.dto;

import java.util.List;

public record QuestionDTO(
    Long id,
    String questionText,
    Integer timeLimitSeconds,
    Integer points,
    Integer orderIndex,
    List<AnswerOptionDTO> options  // Changed from answerOptions to options
) {
}
    