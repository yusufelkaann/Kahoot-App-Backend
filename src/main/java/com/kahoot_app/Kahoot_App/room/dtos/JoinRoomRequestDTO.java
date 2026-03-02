package com.kahoot_app.Kahoot_App.room.dtos;

import jakarta.validation.constraints.NotBlank;

public record JoinRoomRequestDTO(
    @NotBlank(message = "Nickname cannot be blank")
    String nickname
) {
    
}
