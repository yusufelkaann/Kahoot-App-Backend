package com.kahoot_app.Kahoot_App.player.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kahoot_app.Kahoot_App.player.entities.Player;
import com.kahoot_app.Kahoot_App.room.entities.Room;

public interface PlayerRepository extends JpaRepository<Player, Long>{

    List<Player> findByRoom(Room room);

    boolean existsByRoomAndNickname(Room room, String nickname);
    
    
}
