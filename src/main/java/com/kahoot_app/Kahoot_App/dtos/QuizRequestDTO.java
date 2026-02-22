package com.kahoot_app.Kahoot_App.dtos;

import java.util.List;

public record QuizRequestDTO(
    String title,
    String description,
    List<QuestionDTO> questions
) {
}
