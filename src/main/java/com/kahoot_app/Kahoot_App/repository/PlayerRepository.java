package com.kahoot_app.Kahoot_App.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kahoot_app.Kahoot_App.entity.Player;
import com.kahoot_app.Kahoot_App.entity.Room;

public interface PlayerRepository extends JpaRepository<Player, Long>{

    List<Player> findByRoom(Room room);

    boolean existsByRoomAndNickname(Room room, String nickname);
    
    
}
