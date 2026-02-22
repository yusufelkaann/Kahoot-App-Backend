package com.kahoot_app.Kahoot_App.dtos;

import java.util.List;

public record QuestionDTO(
    String questionText,
    Integer timeLimitSeconds,
    Integer points,
    Integer orderIndex,
    List<AnswerOptionDTO> answerOptions
) {
}
    