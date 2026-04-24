package com.kahoot_app.Kahoot_App.repository;

import com.kahoot_app.Kahoot_App.enums.RoomStatus;

public interface GameStatusStore {
    void setGameStatus(String roomCode, RoomStatus status);
    RoomStatus getGameStatus(String roomCode);

    void setCurrentQuestionIndex(String roomCode, int index);
    Integer getCurrentQuestionIndex(String roomCode);
    int getCurrentQuestionIndexSafe(String roomCode);

    void clearRoom(String roomCode);
    void deleteRoomKey(String roomCode);
}
