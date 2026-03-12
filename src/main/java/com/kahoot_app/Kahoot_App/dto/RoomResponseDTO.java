package com.kahoot_app.Kahoot_App.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kahoot_app.Kahoot_App.enums.RoomStatus;

public record RoomResponseDTO(
    Long id,
    String roomCode,
    RoomStatus status,
    int currentQuestionIndex,
    LocalDateTime createdAt,
    List<PlayerResponseDTO> players
) {
    
}
