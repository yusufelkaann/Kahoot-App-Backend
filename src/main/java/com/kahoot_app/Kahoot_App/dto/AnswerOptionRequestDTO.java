package com.kahoot_app.Kahoot_App.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerOptionRequestDTO(
    @NotBlank(message = "Answer option text cannot be blank")
    String text,
    @NotNull(message = "Correct flag must be specified")
    Boolean isCorrect
) {
}
