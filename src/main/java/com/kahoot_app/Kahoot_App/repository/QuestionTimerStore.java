package com.kahoot_app.Kahoot_App.repository;

public interface QuestionTimerStore {
    void startQuestionTimer(String roomCode, int seconds);
    boolean isTimerRunning(String roomCode);
    String generateTimerToken(String roomCode, int questionIndex);
    boolean isTimerTokenValid(String roomCode, String token);
    long getTimerTTL(String roomCode);
}
