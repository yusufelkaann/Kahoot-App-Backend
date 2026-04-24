package com.kahoot_app.Kahoot_App.repository;

import java.util.Map;

public interface ScoreStore {
    void initializeScore(String roomCode, Long playerId);
    void incrementScore(String roomCode, Long playerId, int points);
    int getScore(String roomCode, Long playerId);
    Map<Long, Integer> getAllScores(String roomCode);
}
