package com.kahoot_app.Kahoot_App.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GenerateQuizRequestDTO(

    @NotBlank(message = "Topic cannot be blank")
    String topic,

    @Min(value = 1, message = "Must generate at least 1 question")
    @Max(value = 20, message = "Cannot generate more than 20 questions")
    int questionCount,

    @NotBlank(message = "Difficulty cannot be blank")
    String difficulty
) {}
