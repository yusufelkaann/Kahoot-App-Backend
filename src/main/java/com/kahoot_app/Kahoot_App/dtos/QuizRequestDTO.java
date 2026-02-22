package com.kahoot_app.Kahoot_App.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record QuizRequestDTO(

    @NotBlank(message = "Title is cannot be blank")
    String title,
    String description,

    @NotBlank(message = "Quiz must have at least one question")
    @Valid
    List<QuestionDTO> questions
) {
}
