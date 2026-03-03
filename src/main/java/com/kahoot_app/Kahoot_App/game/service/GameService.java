package com.kahoot_app.Kahoot_App.game.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.global.exceptions.*;
import com.kahoot_app.Kahoot_App.player.entities.Player;
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
        int maxRetries = 5;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                String roomCode = generateRoomCode();

                Room room = new Room();
                room.setRoomCode(roomCode);
                room.setStatus(RoomStatus.WAITING);
                room.setCurrentQuestionIndex(0);
                room.setCreatedAt(LocalDateTime.now());

                return roomRepository.save(room);
            } catch (DataIntegrityViolationException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Failed to generate unique room code");
                }
                // Retry with a new code
            }
        }
        
        throw new RuntimeException("Failed to create room");
    }

    // Assign quiz to the room
    @Transactional
    public Room assignQuiz(String roomCode, Long quizId) {
        Room room = getRoomByCode(roomCode);


        // check room status
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ConflictException("Cannot assign quiz after game started");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));
        
        room.setQuiz(quiz);

        return room;
    }


    // Start game
    @Transactional
    public void startGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ConflictException("Game already started");
        }

        if (room.getQuiz() == null) {
            throw new BadRequestException("Cannot start game without quiz");
        }

        if (room.getQuiz().getQuestions().isEmpty()) {
            throw new BadRequestException("Quiz has no questions");
        }

        if (room.getPlayers() == null || room.getPlayers().isEmpty()) {
            throw new BadRequestException("No players in room");
        }

        room.setStatus(RoomStatus.STARTED);

        room.setCurrentQuestionIndex(0);
    }

    @Transactional
    public void finishGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getStatus() != RoomStatus.STARTED) {
            throw new ConflictException("Game not started");
        }

        room.setStatus(RoomStatus.FINISHED);
    }

    @Transactional
    public Player joinRoom(String roomCode, String nickname) {

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ConflictException("Game already started");
        }

        boolean nicknameExists =
                playerRepository.existsByRoomAndNickname(room, nickname);

        if (nicknameExists) {
            throw new ConflictException("Nickname already taken");
        }

        Player player = new Player(nickname, room);

        // IMPORTANT: use helper method to keep both sides in sync
        room.addPlayer(player);

        return playerRepository.save(player);
    }

    // HELPERS
    @Transactional(readOnly = true)
    public Room getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }


    
    /**
     * Generates a random 6-digit room code.
     * Uniqueness is enforced by database constraint with retry logic in createRoom().
     */
    private String generateRoomCode() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
