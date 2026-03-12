package com.kahoot_app.Kahoot_App.service;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kahoot_app.Kahoot_App.dto.RoomResponseDTO;

@Service
public class GameWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    
    public GameWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastGameUpdate(String roomCode, RoomResponseDTO roomData) {
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, roomData);
    }

    public void broadcastQuestionAdvance(String roomCode, int questionIndex) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomCode + "/question", questionIndex
        );
    }

    public void broadcastScoreUpdate(String roomCode, Long playerId, int score) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomCode + "/scores",
            Map.of("playerId", playerId, "score", score)
        );
    }

    public void broadcastGameFinished(String roomCode) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomCode + "/finish",
            Map.of("status", "FINISHED")
        );
    }
}
