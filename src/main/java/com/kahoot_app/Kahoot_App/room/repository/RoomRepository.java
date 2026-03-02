package com.kahoot_app.Kahoot_App.room.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kahoot_app.Kahoot_App.room.entities.Room;
import com.kahoot_app.Kahoot_App.room.enums.RoomStatus;

import java.util.List;


public interface RoomRepository extends JpaRepository<Room, Long>{

    Optional<Room> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);

    boolean existsByQuizIdAndStatus(Long quizId, RoomStatus status);

    
}
