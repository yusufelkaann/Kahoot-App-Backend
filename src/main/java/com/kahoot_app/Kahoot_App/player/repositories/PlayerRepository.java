package com.kahoot_app.Kahoot_App.player.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kahoot_app.Kahoot_App.player.entities.Player;
import com.kahoot_app.Kahoot_App.room.entities.Room;

public interface PlayerRepository extends JpaRepository<Player, UUID>{

    List<Player> findByRoom(Room room);

    boolean exissByRoomAndNickname(Room room, String nickname);
    
    
}
