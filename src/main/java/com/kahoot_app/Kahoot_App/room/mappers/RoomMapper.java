package com.kahoot_app.Kahoot_App.room.mappers;

import java.util.stream.Collectors;

import com.kahoot_app.Kahoot_App.player.dtos.PlayerResponseDTO;
import com.kahoot_app.Kahoot_App.player.entities.Player;
import com.kahoot_app.Kahoot_App.room.dtos.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.room.entities.Room;


public class RoomMapper {
    // entity to DTO
    public static RoomResponseDTO toRoomResponseDTO(Room room) {
        var players = room.getPlayers()
                        .stream()
                        .map(RoomMapper::toPlayerResponseDTO)
                        .collect(Collectors.toList());
        
        return new RoomResponseDTO(
                room.getId(),
                room.getRoomCode(),
                room.getStatus(),
                room.getCurrentQuestionIndex(),
                room.getCreatedAt(),
                players
        );
        
                          
    }

    public static PlayerResponseDTO toPlayerResponseDTO(Player player) {
        return new PlayerResponseDTO(
                player.getId(),
                player.getNickname(),
                player.getScore()
        );
    }
}
