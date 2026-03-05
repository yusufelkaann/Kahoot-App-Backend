package com.kahoot_app.Kahoot_App.room.mappers;

import java.util.stream.Collectors;

import com.kahoot_app.Kahoot_App.player.dtos.PlayerResponseDTO;
import com.kahoot_app.Kahoot_App.player.entities.Player;
import com.kahoot_app.Kahoot_App.redis.service.RedisGameStateService;
import com.kahoot_app.Kahoot_App.room.dtos.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.room.entities.Room;


public class RoomMapper {
    // entity to DTO
    public static RoomResponseDTO toRoomResponseDTO(Room room, RedisGameStateService redisService) {
        var players = room.getPlayers()
                        .stream()
                        .map(player -> toPlayerResponseDTO(player, room.getRoomCode(), redisService))
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

    public static PlayerResponseDTO toPlayerResponseDTO(Player player, String roomCode, RedisGameStateService redisService) {
        // Fetch live score from Redis if available, fallback to Player entity score
        Integer redisScore = redisService.getScore(roomCode, player.getId());
        int score = (redisScore != null) ? redisScore : player.getScore();
        
        return new PlayerResponseDTO(
                player.getId(),
                player.getNickname(),
                score
        );
    }
}
