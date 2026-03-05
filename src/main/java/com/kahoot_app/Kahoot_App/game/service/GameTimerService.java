package com.kahoot_app.Kahoot_App.game.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.quiz.entities.Question;
import com.kahoot_app.Kahoot_App.quiz.entities.Quiz;
import com.kahoot_app.Kahoot_App.quiz.repository.QuizRepository;
import com.kahoot_app.Kahoot_App.redis.service.RedisGameStateService;

/**
 * Separate service to handle async timer operations.
 * This is necessary because @Async does not work with self-invocation within the same class.
 */
@Service
public class GameTimerService {
    
    private final RedisGameStateService redisGameStateService;
    private final QuizRepository quizRepository;
    private final GameService gameService;

    public GameTimerService(
            RedisGameStateService redisGameStateService,
            QuizRepository quizRepository,
            @Lazy GameService gameService) {
        this.redisGameStateService = redisGameStateService;
        this.quizRepository = quizRepository;
        this.gameService = gameService;
    }

    
    @Async
    public void startQuestionTimer(String roomCode, int expectedQuestionIndex, String timerToken, int timeLimitSeconds) {

        

       

        redisGameStateService.startQuestionTimer(roomCode, timeLimitSeconds);

        try {
            Thread.sleep(timeLimitSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return; // Don't advance question if timer was interrupted
        }

        // Verify timer token 
        if (!redisGameStateService.isTimerTokenValid(roomCode, timerToken)) {
            return; 
        }

        // Advance question after timer ends
        gameService.advanceQuestionAutomaticallyByRoomCode(roomCode);
    }

}
