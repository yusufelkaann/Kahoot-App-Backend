package com.kahoot_app.Kahoot_App.redis.service;

import com.kahoot_app.Kahoot_App.room.enums.RoomStatus;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisGameStateService {
    
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisGameStateService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate; 
    }

    // Helpers
    private String roomKey(String roomCode) {
        return "room:" + roomCode;
    }

    private String scoreKey(String roomCode) {
        return "room:" + roomCode + ":score";
    }

    private String answerKey(String roomCode, int questionIndex) {
        return "room:" + roomCode + ":answers:" + questionIndex;
    }


    // Room state
    public void setGameStatus(String roomCode, RoomStatus status) {
        redisTemplate.opsForHash().put(roomKey(roomCode), "status", status);
    }

    public RoomStatus getGameStatus(String roomCode) {
        Object val = redisTemplate.opsForHash().get(roomKey(roomCode), "status");
        return val != null ? RoomStatus.valueOf(val.toString()) : null;
    }

    public void setCurrentQuestionIndex(String roomCode, int index) {
        redisTemplate.opsForHash().put(roomKey(roomCode), "currentQuestionIndex", index);
    }

    public Integer getCurrentQuestionIndex(String roomCode) {
        Object val = redisTemplate.opsForHash().get(roomKey(roomCode), "currentQuestionIndex");
        return val != null ? Integer.parseInt(val.toString()) : null;
    }

    // Scores
    public void initializeScore(String roomCode, Long playerId) {
        redisTemplate.opsForHash().put(scoreKey(roomCode), playerId.toString(), 0);
    }

    public void incrementScore(String roomCode, Long playerId, int points) {
        redisTemplate.opsForHash().increment(scoreKey(roomCode), playerId.toString(), points);
    }

    public Integer getScore(String roomCode, Long playerId) {
        Object val = redisTemplate.opsForHash().get(scoreKey(roomCode), playerId.toString());
        return val != null ? Integer.parseInt(val.toString()) : 0;
    }


    // Answers
    public void saveAnswer(String roomCode, int questionIndex, Long playerId, Long answerOptionId) {
        redisTemplate.opsForHash().put(answerKey(roomCode, questionIndex), playerId.toString(), answerOptionId);
    }

    public boolean hasAnswered(String roomCode, int questionIndex, Long playerId) {
        return redisTemplate.opsForHash().hasKey(answerKey(roomCode, questionIndex), playerId.toString());
    }

    public Long getAnswerCount(String roomCode, int questionIndex) {
        return redisTemplate.opsForHash().size(answerKey(roomCode, questionIndex));
    }

    // Timer
    public void startQuestionTimer(String roomCode, int seconds) {
        redisTemplate.opsForValue().set(
            "room:" + roomCode + ":timer",
            "running",
            seconds,
            TimeUnit.SECONDS
        );
    }

    public boolean isTimerRunning(String roomCode) {
        return redisTemplate.hasKey("room:" + roomCode + ":timer");
    }

    // Cleanup
    public void clearRoom(String roomCode) {
        redisTemplate.delete(roomKey(roomCode));
        redisTemplate.delete(scoreKey(roomCode));
    }

}
