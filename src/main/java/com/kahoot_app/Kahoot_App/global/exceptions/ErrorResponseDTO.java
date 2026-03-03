package com.kahoot_app.Kahoot_App.global.exceptions;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {}