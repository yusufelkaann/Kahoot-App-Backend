package com.kahoot_app.Kahoot_App.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kahoot_app.Kahoot_App.repository.QuestionTimerStore;

@Service
public class GameTimerService {

    private final QuestionTimerStore questionTimerStore;
    private final GameFlowService gameFlowService;

    public GameTimerService(
            QuestionTimerStore questionTimerStore,
            @Lazy GameFlowService gameFlowService) {
        this.questionTimerStore = questionTimerStore;
        this.gameFlowService = gameFlowService;
    }

    @Async
    public void startQuestionTimer(String roomCode, int expectedQuestionIndex, String timerToken, int timeLimitSeconds) {
        questionTimerStore.startQuestionTimer(roomCode, timeLimitSeconds);

        try {
            Thread.sleep(timeLimitSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!questionTimerStore.isTimerTokenValid(roomCode, timerToken)) {
            return;
        }

        gameFlowService.advanceQuestionAutomaticallyByRoomCode(roomCode);
    }
}
