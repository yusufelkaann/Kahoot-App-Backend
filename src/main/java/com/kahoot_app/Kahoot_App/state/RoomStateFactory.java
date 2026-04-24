package com.kahoot_app.Kahoot_App.state;

import com.kahoot_app.Kahoot_App.enums.RoomStatus;

public class RoomStateFactory {

    public static RoomState forStatus(RoomStatus status) {
        if (status == null) {
            return new FinishedState();
        }
        return switch (status) {
            case WAITING -> new WaitingState();
            case STARTED -> new StartedState();
            case FINISHED -> new FinishedState();
        };
    }
}
