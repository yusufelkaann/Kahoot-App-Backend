package com.kahoot_app.Kahoot_App.repository;

public interface AnswerStore {
    void saveAnswer(String roomCode, int questionIndex, Long playerId, Long answerOptionId);
    boolean hasAnswered(String roomCode, int questionIndex, Long playerId);
    Long getAnswerCount(String roomCode, int questionIndex);
}
