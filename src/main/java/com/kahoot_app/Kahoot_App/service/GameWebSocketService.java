package com.kahoot_app.Kahoot_App.service;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kahoot_app.Kahoot_App.events.GameFinishedEvent;
import com.kahoot_app.Kahoot_App.events.QuestionAdvancedEvent;

@Service
public class GameWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onQuestionAdvanced(QuestionAdvancedEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + event.getRoomCode() + "/question", event.getQuestionIndex()
        );
    }

    @EventListener
    public void onGameFinished(GameFinishedEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + event.getRoomCode() + "/finish",
            Map.of("status", "FINISHED")
        );
    }

    public void broadcastScoreUpdate(String roomCode, Long playerId, int score) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomCode + "/scores",
            Map.of("playerId", playerId, "score", score)
        );
    }
}
