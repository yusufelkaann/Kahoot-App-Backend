package com.kahoot_app.Kahoot_App.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequestDTO(
    @NotBlank(message = "Host nickname is required")
    String hostNickname
) {
}
