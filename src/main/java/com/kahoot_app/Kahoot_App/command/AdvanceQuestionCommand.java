package com.kahoot_app.Kahoot_App.command;

import com.kahoot_app.Kahoot_App.service.GameFlowService;

class AdvanceQuestionCommand implements GameCommand {

    private final String roomCode;
    private final long playerId;
    private final GameFlowService gameFlowService;

    AdvanceQuestionCommand(String roomCode, long playerId, GameFlowService gameFlowService) {
        this.roomCode = roomCode;
        this.playerId = playerId;
        this.gameFlowService = gameFlowService;
    }

    @Override
    public void execute() {
        gameFlowService.advanceQuestionManually(roomCode, playerId);
    }
}
