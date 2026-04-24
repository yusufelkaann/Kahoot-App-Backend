package com.kahoot_app.Kahoot_App.command;

import org.springframework.stereotype.Component;

import com.kahoot_app.Kahoot_App.service.GameFlowService;

@Component
public class GameCommandFactory {

    private final GameFlowService gameFlowService;

    public GameCommandFactory(GameFlowService gameFlowService) {
        this.gameFlowService = gameFlowService;
    }

    public GameCommand startGame(String roomCode) {
        return new StartGameCommand(roomCode, gameFlowService);
    }

    public GameCommand finishGame(String roomCode) {
        return new FinishGameCommand(roomCode, gameFlowService);
    }

    public GameCommand advanceQuestion(String roomCode, long playerId) {
        return new AdvanceQuestionCommand(roomCode, playerId, gameFlowService);
    }
}
