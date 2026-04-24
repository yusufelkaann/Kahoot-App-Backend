package com.kahoot_app.Kahoot_App.command;

import com.kahoot_app.Kahoot_App.service.GameFlowService;

class StartGameCommand implements GameCommand {

    private final String roomCode;
    private final GameFlowService gameFlowService;

    StartGameCommand(String roomCode, GameFlowService gameFlowService) {
        this.roomCode = roomCode;
        this.gameFlowService = gameFlowService;
    }

    @Override
    public void execute() {
        gameFlowService.startGame(roomCode);
    }
}
