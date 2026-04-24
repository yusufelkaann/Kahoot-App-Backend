package com.kahoot_app.Kahoot_App.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.dto.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.entity.Player;
import com.kahoot_app.Kahoot_App.entity.Quiz;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.enums.PlayerRole;
import com.kahoot_app.Kahoot_App.enums.RoomStatus;
import com.kahoot_app.Kahoot_App.global.exceptions.ConflictException;
import com.kahoot_app.Kahoot_App.global.exceptions.NotFoundException;
import com.kahoot_app.Kahoot_App.state.RoomStateFactory;
import com.kahoot_app.Kahoot_App.mappers.RoomMapper;
import com.kahoot_app.Kahoot_App.repository.PlayerRepository;
import com.kahoot_app.Kahoot_App.repository.QuizRepository;
import com.kahoot_app.Kahoot_App.repository.RoomRepository;
import com.kahoot_app.Kahoot_App.repository.ScoreStore;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final QuizRepository quizRepository;
    private final PlayerRepository playerRepository;
    private final ScoreStore scoreStore;

    private final Random random = new Random();

    public RoomService(RoomRepository roomRepository,
            QuizRepository quizRepository,
            PlayerRepository playerRepository,
            ScoreStore scoreStore) {
        this.roomRepository = roomRepository;
        this.quizRepository = quizRepository;
        this.playerRepository = playerRepository;
        this.scoreStore = scoreStore;
    }

    @Transactional
    public Room createRoom(String playerNickName) {
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

                Player hostPlayer = new Player(playerNickName, room, PlayerRole.HOST);
                room.addPlayer(hostPlayer);

                return roomRepository.save(room);
            } catch (DataIntegrityViolationException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Failed to generate unique room code");
                }
            }
        }

        throw new RuntimeException("Failed to create room");
    }

    @Transactional
    public Room assignQuiz(String roomCode, Long quizId) {
        Room room = getRoomByCode(roomCode);

        RoomStateFactory.forStatus(room.getStatus()).onAssignQuiz();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        room.setQuiz(quiz);
        return room;
    }

    @Transactional
    public Player joinRoom(String roomCode, String nickname) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        RoomStateFactory.forStatus(room.getStatus()).onJoin();

        if (playerRepository.existsByRoomAndNickname(room, nickname)) {
            throw new ConflictException("Nickname already taken");
        }

        Player player = new Player(nickname, room, PlayerRole.PLAYER);
        room.addPlayer(player);
        Player savedPlayer = playerRepository.save(player);
        scoreStore.initializeScore(roomCode, savedPlayer.getId());

        return savedPlayer;
    }

    @Transactional(readOnly = true)
    public Room getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomResponseDTO(String roomCode) {
        Room room = getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room, scoreStore.getAllScores(roomCode));
    }

    private String generateRoomCode() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
