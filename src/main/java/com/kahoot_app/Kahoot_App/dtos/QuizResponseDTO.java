package com.kahoot_app.Kahoot_App.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResponseDTO(
    Long id,
    String title,
    String description,
    LocalDateTime createdAt,
    List<QuestionDTO> questions
) {
}
