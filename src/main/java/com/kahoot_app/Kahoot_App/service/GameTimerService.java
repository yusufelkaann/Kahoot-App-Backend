package com.kahoot_app.Kahoot_App.service;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kahoot_app.Kahoot_App.events.QuestionAdvancedEvent;
import com.kahoot_app.Kahoot_App.repository.QuestionTimerStore;

@Service
public class GameTimerService {

    private final QuestionTimerStore questionTimerStore;
    private final GameFlowService gameFlowService;

    public GameTimerService(QuestionTimerStore questionTimerStore, GameFlowService gameFlowService) {
        this.questionTimerStore = questionTimerStore;
        this.gameFlowService = gameFlowService;
    }

    @Async
    @EventListener
    public void onQuestionAdvanced(QuestionAdvancedEvent event) {
        questionTimerStore.startQuestionTimer(event.getRoomCode(), event.getTimeLimitSeconds());

        try {
            Thread.sleep(event.getTimeLimitSeconds() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!questionTimerStore.isTimerTokenValid(event.getRoomCode(), event.getTimerToken())) {
            return;
        }

        gameFlowService.advanceQuestionAutomaticallyByRoomCode(event.getRoomCode());
    }
}
