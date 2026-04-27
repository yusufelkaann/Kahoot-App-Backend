package com.kahoot_app.Kahoot_App.events;

public class QuestionAdvancedEvent {

    private final String roomCode;
    private final int questionIndex;
    private final String timerToken;
    private final int timeLimitSeconds;

    public QuestionAdvancedEvent(String roomCode, int questionIndex, String timerToken, int timeLimitSeconds) {
        this.roomCode = roomCode;
        this.questionIndex = questionIndex;
        this.timerToken = timerToken;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public String getRoomCode() { return roomCode; }
    public int getQuestionIndex() { return questionIndex; }
    public String getTimerToken() { return timerToken; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
}
