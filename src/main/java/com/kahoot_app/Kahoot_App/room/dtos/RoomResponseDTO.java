package com.kahoot_app.Kahoot_App.room.dtos;

import com.kahoot_app.Kahoot_App.player.dtos.*;
import java.time.LocalDateTime;
import java.util.List;

import com.kahoot_app.Kahoot_App.room.enums.RoomStatus;

public record RoomResponseDTO(
    Long id,
    String roomCode,
    RoomStatus status,
    int currentQuestionIndex,
    LocalDateTime createdAt,
    List<PlayerResponseDTO> players
) {
    
}
