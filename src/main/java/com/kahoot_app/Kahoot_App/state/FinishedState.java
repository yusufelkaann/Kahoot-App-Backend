package com.kahoot_app.Kahoot_App.state;

import com.kahoot_app.Kahoot_App.global.exceptions.ConflictException;

public class FinishedState implements RoomState {

    @Override
    public void onJoin() {
        throw new ConflictException("Game is already finished");
    }

    @Override
    public void onAssignQuiz() {
        throw new ConflictException("Game is already finished");
    }

    @Override
    public void onStart() {
        throw new ConflictException("Game is already finished");
    }

    @Override
    public void onAdvance() {
        throw new ConflictException("Game is already finished");
    }

    @Override
    public void onSubmitAnswer() {
        throw new ConflictException("Game is already finished");
    }

    @Override
    public void onFinish() {
        throw new ConflictException("Game is already finished");
    }
}
