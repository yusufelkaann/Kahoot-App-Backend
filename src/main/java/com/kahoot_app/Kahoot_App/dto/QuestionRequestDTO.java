package com.kahoot_app.Kahoot_App.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record QuestionRequestDTO(
    @NotBlank(message = "Question text cannot be blank")
    String questionText,
    Integer timeLimitSeconds,
    Integer points,
    Integer orderIndex,

    @NotEmpty(message = "Answer options cannot be empty")
    @Valid
    List<AnswerOptionRequestDTO> answerOptions
) {
}
