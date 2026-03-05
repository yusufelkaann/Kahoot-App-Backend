package com.kahoot_app.Kahoot_App.game.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kahoot_app.Kahoot_App.quiz.entities.Question;
import com.kahoot_app.Kahoot_App.quiz.entities.Quiz;
import com.kahoot_app.Kahoot_App.redis.service.RedisGameStateService;
import com.kahoot_app.Kahoot_App.room.entities.Room;

/**
 * Separate service to handle async timer operations.
 */
@Service
public class GameTimerService {
    
    private final RedisGameStateService redisGameStateService;
    private final GameService gameService;

    public GameTimerService(RedisGameStateService redisGameStateService, @Lazy GameService gameService) {
        this.redisGameStateService = redisGameStateService;
        this.gameService = gameService;
    }

    @Async
    public void startQuestionTimer(Room room, Quiz quiz) {

        int questionIndex = redisGameStateService.getCurrentQuestionIndex(room.getRoomCode());

        Question question = quiz.getQuestions().get(questionIndex);

        int timeLimit = question.getTimeLimitSeconds();

        redisGameStateService.startQuestionTimer(room.getRoomCode(), timeLimit);

        try {
            Thread.sleep(timeLimit * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return; // Don't advance question if timer was interrupted
        }

        // advance question after timer ends
        gameService.advanceQuestionAutomatically(room, quiz);
    }
}
