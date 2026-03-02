package com.kahoot_app.Kahoot_App.game.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.player.repositories.PlayerRepository;
import com.kahoot_app.Kahoot_App.quiz.entities.Quiz;
import com.kahoot_app.Kahoot_App.quiz.repository.QuizRepository;
import com.kahoot_app.Kahoot_App.room.entities.Room;
import com.kahoot_app.Kahoot_App.room.enums.RoomStatus;
import com.kahoot_app.Kahoot_App.room.repository.RoomRepository;

@Service
public class GameService {
    private final RoomRepository roomRepository;
    private final QuizRepository quizRepository;
    private final PlayerRepository playerRepository;

    private final Random random = new Random();

    public GameService(RoomRepository roomRepository, 
        QuizRepository quizRepository,
        PlayerRepository playerRepository) {
        this.roomRepository = roomRepository;
        this.quizRepository = quizRepository;
        this.playerRepository = playerRepository;
    }


    // Create empty room
    @Transactional
    public Room createRoom() {
        String roomCode = generateUniqueRoomCode();

        Room room = new Room();
        room.setRoomCode(roomCode);
        room.setStatus(RoomStatus.WAITING);
        room.setCurrentQuestionIndex(0);
        room.setCreatedAt(LocalDateTime.now());

        return roomRepository.save(room);
    }

    // Assign quiz to the room
    @Transactional
    public Room assignQuiz(String roomCode, Long quizId) {
        Room room = getRoomByCode(roomCode);


        // check room status
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new IllegalStateException("Cannot assign quiz after game started");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        room.setQuiz(quiz);

        return room;
    }


    // Start game
    @Transactional
    public void starGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new IllegalStateException("Game already started");
        }

        if (room.getQuiz() == null) {
            throw new IllegalStateException("Cannot start game without quiz");
        }

        if (room.getQuiz().getQuestions().isEmpty()) {
            throw new IllegalStateException("Quiz has no questions");
        }

        if (room.getPlayers() == null || room.getPlayers().isEmpty()) {
            throw new IllegalStateException("No players in room");
        }

        room.setStatus(RoomStatus.STARTED);

        room.setCurrentQuestionIndex(0);
    }

    @Transactional
    public void finishGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getStatus() != RoomStatus.STARTED) {
            throw new IllegalStateException("Game not started");
        }

        room.setStatus(RoomStatus.FINISHED);
    }

    // HELPERS
    private Room getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }


    private String generateUniqueRoomCode() {
        String code;
        do {
            int number = 100000 + random.nextInt(900000);
            code = String.valueOf(number);
        } while (roomRepository.existsByRoomCode(code));
        return code;
    }
}
