package com.kahoot_app.Kahoot_App.repository;

import java.util.Map;

import com.kahoot_app.Kahoot_App.enums.RoomStatus;

public interface GameSessionStateStore {

    void setGameStatus(String roomCode, RoomStatus status);
    RoomStatus getGameStatus(String roomCode);

    void setCurrentQuestionIndex(String roomCode, int index);
    Integer getCurrentQuestionIndex(String roomCode);
    int getCurrentQuestionIndexSafe(String roomCode);

    void initializeScore(String roomCode, Long playerId);
    void incrementScore(String roomCode, Long playerId, int points);
    int getScore(String roomCode, Long playerId);
    Map<Long, Integer> getAllScores(String roomCode);

    void saveAnswer(String roomCode, int questionIndex, Long playerId, Long answerOptionId);
    boolean hasAnswered(String roomCode, int questionIndex, Long playerId);
    Long getAnswerCount(String roomCode, int questionIndex);

    void startQuestionTimer(String roomCode, int seconds);
    boolean isTimerRunning(String roomCode);
    String generateTimerToken(String roomCode, int questionIndex);
    boolean isTimerTokenValid(String roomCode, String token);
    long getTimerTTL(String roomCode);

    void clearRoom(String roomCode);
    void deleteRoomKey(String roomCode);
}
