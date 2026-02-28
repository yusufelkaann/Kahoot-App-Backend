package com.kahoot_app.Kahoot_App.dtos;

import java.util.List;

public record QuestionResponseDTO(
    Long id,
    String questionText,
    Integer timeLimitSeconds,
    Integer points,
    Integer orderIndex,
    List<AnswerOptionResponseDTO> options
) {
    
}
