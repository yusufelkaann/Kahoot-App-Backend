package com.kahoot_app.Kahoot_App.room.mappers;

import java.util.Map;
import java.util.stream.Collectors;

import com.kahoot_app.Kahoot_App.player.dtos.PlayerResponseDTO;
import com.kahoot_app.Kahoot_App.player.entities.Player;
import com.kahoot_app.Kahoot_App.redis.service.RedisGameStateService;
import com.kahoot_app.Kahoot_App.room.dtos.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.room.entities.Room;


public class RoomMapper {
    // entity to DTO
    public static RoomResponseDTO toRoomResponseDTO(Room room, RedisGameStateService redisService) {
        // Fetch all scores in one Redis call
        Map<Long, Integer> allScores = redisService.getAllScores(room.getRoomCode());
        
        var players = room.getPlayers()
                        .stream()
                        .map(player -> toPlayerResponseDTO(player, allScores))
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

    public static PlayerResponseDTO toPlayerResponseDTO(Player player, Map<Long, Integer> scoreMap) {
        // Fetch score from the pre-loaded map, fallback to Player entity score
        Integer score = scoreMap.getOrDefault(player.getId(), player.getScore());
        
        return new PlayerResponseDTO(
                player.getId(),
                player.getNickname(),
                score
        );
    }
}
