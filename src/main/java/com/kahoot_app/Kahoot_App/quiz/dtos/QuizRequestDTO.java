package com.kahoot_app.Kahoot_App.quiz.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record QuizRequestDTO(

    @NotBlank(message = "Title is cannot be blank")
    String title,
    String description,

    @NotEmpty(message = "Quiz must have at least one question")
    @Valid
    List<QuestionDTO> questions
) {
}
