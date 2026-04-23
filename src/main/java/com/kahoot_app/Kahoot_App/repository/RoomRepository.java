package com.kahoot_app.Kahoot_App.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.enums.RoomStatus;



public interface RoomRepository extends JpaRepository<Room, Long>{

    Optional<Room> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);

    boolean existsByQuizIdAndStatus(Long quizId, RoomStatus status);

    Optional<Room> findByQuizIdAndStatus(Long quizId, RoomStatus status);
}
