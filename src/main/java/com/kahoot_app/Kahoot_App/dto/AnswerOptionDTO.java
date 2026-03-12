package com.kahoot_app.Kahoot_App.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerOptionDTO(
    Long id,  
    @NotBlank(message = "Answer option text cannot be blank")
    String text,
    Boolean isCorrect 
) {
}
