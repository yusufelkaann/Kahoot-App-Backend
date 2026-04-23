package com.kahoot_app.Kahoot_App.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kahoot_app.Kahoot_App.repository.GameSessionStateStore;


/**
 * Separate service to handle async timer operations.
 * This is necessary because @Async does not work with self-invocation within the same class.
 */
@Service
public class GameTimerService {

    private final GameSessionStateStore gameSessionStateStore;
    private final GameService gameService;

    public GameTimerService(
            GameSessionStateStore gameSessionStateStore,
            @Lazy GameService gameService) {
        this.gameSessionStateStore = gameSessionStateStore;
        this.gameService = gameService;
    }

    
    @Async
    public void startQuestionTimer(String roomCode, int expectedQuestionIndex, String timerToken, int timeLimitSeconds) {

        

       

        gameSessionStateStore.startQuestionTimer(roomCode, timeLimitSeconds);

        try {
            Thread.sleep(timeLimitSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return; // Don't advance question if timer was interrupted
        }

        // Verify timer token 
        if (!gameSessionStateStore.isTimerTokenValid(roomCode, timerToken)) {
            return; 
        }

        // Advance question after timer ends
        gameService.advanceQuestionAutomaticallyByRoomCode(roomCode);
    }

}
