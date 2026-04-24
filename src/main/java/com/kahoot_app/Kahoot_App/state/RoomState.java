package com.kahoot_app.Kahoot_App.state;

public interface RoomState {
    void onJoin();
    void onAssignQuiz();
    void onStart();
    void onAdvance();
    void onSubmitAnswer();
    void onFinish();
}
