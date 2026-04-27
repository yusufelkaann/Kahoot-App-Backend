package com.kahoot_app.Kahoot_App.events;

public class GameFinishedEvent {

    private final String roomCode;

    public GameFinishedEvent(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomCode() { return roomCode; }
}
