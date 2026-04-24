package com.kahoot_app.Kahoot_App.state;

import com.kahoot_app.Kahoot_App.global.exceptions.ConflictException;

public class StartedState implements RoomState {

    @Override
    public void onJoin() {
        throw new ConflictException("Game already started");
    }

    @Override
    public void onAssignQuiz() {
        throw new ConflictException("Cannot assign quiz after game started");
    }

    @Override
    public void onStart() {
        throw new ConflictException("Game already started");
    }

    @Override
    public void onAdvance() {}

    @Override
    public void onSubmitAnswer() {}

    @Override
    public void onFinish() {}
}
