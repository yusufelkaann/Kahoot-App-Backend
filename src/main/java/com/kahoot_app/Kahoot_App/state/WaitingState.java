package com.kahoot_app.Kahoot_App.state;

import com.kahoot_app.Kahoot_App.global.exceptions.ConflictException;

public class WaitingState implements RoomState {

    @Override
    public void onJoin() {}

    @Override
    public void onAssignQuiz() {}

    @Override
    public void onStart() {}

    @Override
    public void onAdvance() {
        throw new ConflictException("Game has not started yet");
    }

    @Override
    public void onSubmitAnswer() {
        throw new ConflictException("Game has not started yet");
    }

    @Override
    public void onFinish() {
        throw new ConflictException("Game has not started yet");
    }
}
