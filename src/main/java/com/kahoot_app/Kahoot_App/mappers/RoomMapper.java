package com.kahoot_app.Kahoot_App.mappers;

import java.util.Map;
import java.util.stream.Collectors;

import com.kahoot_app.Kahoot_App.dto.PlayerResponseDTO;
import com.kahoot_app.Kahoot_App.dto.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.entity.Player;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.service.RedisGameStateService;


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
