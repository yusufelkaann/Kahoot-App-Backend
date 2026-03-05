package com.kahoot_app.Kahoot_App.game.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.global.exceptions.*;
import com.kahoot_app.Kahoot_App.player.entities.Player;
import com.kahoot_app.Kahoot_App.player.enums.PlayerRole;
import com.kahoot_app.Kahoot_App.player.repositories.PlayerRepository;
import com.kahoot_app.Kahoot_App.quiz.entities.AnswerOption;
import com.kahoot_app.Kahoot_App.quiz.entities.Question;
import com.kahoot_app.Kahoot_App.quiz.entities.Quiz;
import com.kahoot_app.Kahoot_App.quiz.repository.QuizRepository;
import com.kahoot_app.Kahoot_App.redis.service.RedisGameStateService;
import com.kahoot_app.Kahoot_App.room.entities.Room;
import com.kahoot_app.Kahoot_App.room.enums.RoomStatus;
import com.kahoot_app.Kahoot_App.room.repository.RoomRepository;

@Service
public class GameService {
    private final RoomRepository roomRepository;
    private final QuizRepository quizRepository;
    private final PlayerRepository playerRepository;

    private final RedisGameStateService redisGameStateService;

    private final Random random = new Random();

    public GameService(RoomRepository roomRepository, 
        QuizRepository quizRepository,
        PlayerRepository playerRepository,
        RedisGameStateService redisGameStateService) {
        this.roomRepository = roomRepository;
        this.quizRepository = quizRepository;
        this.playerRepository = playerRepository;
        this.redisGameStateService = redisGameStateService;
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

                Player hostPlayer = new Player(roomCode, room, PlayerRole.HOST);
                playerRepository.save(hostPlayer);

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
        redisGameStateService.setGameStatus(roomCode, RoomStatus.STARTED);
        redisGameStateService.setCurrentQuestionIndex(roomCode, 0);

        startQuestionTimer(room, room.getQuiz());

        room.setCurrentQuestionIndex(0);
    }

    @Async
    public void startQuestionTimer(Room room, Quiz quiz) {

        int questionIndex = redisGameStateService.getCurrentQuestionIndex(room.getRoomCode());

        Question question = quiz.getQuestions().get(questionIndex);

        int timeLimit = question.getTimeLimitSeconds();

        redisGameStateService.startQuestionTimer(room.getRoomCode(), timeLimit);

        try {
            Thread.sleep(timeLimit * 1000L);
        } catch (InterruptedException e) {
            
        }

        // advance question after timer ends
        advanceQuestionAutomatically(room, quiz);
    }

    @Transactional
    public void finishGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getStatus() != RoomStatus.STARTED) {
            throw new ConflictException("Game not started");
        }

        redisGameStateService.setGameStatus(roomCode, RoomStatus.FINISHED);
        redisGameStateService.clearRoom(roomCode);
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

        Player player = new Player(nickname, room, PlayerRole.PLAYER);

        
        room.addPlayer(player);
        redisGameStateService.initializeScore(roomCode, player.getId());

        return playerRepository.save(player);
    }

    @Transactional
    public void submitAnswer(Room room, Player player, Long answerOptionID) {
        RoomStatus status = redisGameStateService.getGameStatus(room.getRoomCode());
        if (status != RoomStatus.STARTED) {
            throw new BadRequestException("Game has not started yet");
        }

        if (player.getRole() == PlayerRole.HOST) {
            throw new BadRequestException("Host cannot answer questions");
        }

        int currentQuestion = redisGameStateService.getCurrentQuestionIndex(room.getRoomCode());
        Question question = room.getQuiz().getQuestions().get(currentQuestion);

        if (redisGameStateService.hasAnswered(room.getRoomCode(), currentQuestion, player.getId())) {
            throw new BadRequestException("Player already answered this question");
        }

        redisGameStateService.saveAnswer(room.getRoomCode(), currentQuestion, player.getId(), answerOptionID);

        calculateAndApplyScore(room, player, question, answerOptionID);
    }

    @Transactional 
    public void advanceQuestionManually(Room room, Player player, Quiz quiz) {
        if (player.getRole() != PlayerRole.HOST) {
            throw new BadRequestException("Player cannot change the question");
        }

        int index = redisGameStateService.getCurrentQuestionIndex(room.getRoomCode());
        checkIfLastQuestion(room, quiz, index);

        redisGameStateService.setCurrentQuestionIndex(room.getRoomCode(), index + 1);
        startQuestionTimer(room, quiz);
    }

    // HELPERS
    @Transactional(readOnly = true)
    public Room getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    private void calculateAndApplyScore(
            Room room,
            Player player,
            Question question,
            Long optionId
    ) {

        AnswerOption option = question.getOptions()
                .stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Answer option not found"));

        if (option.getIsCorrect()) {

            int points = question.getPoints();

            redisGameStateService.incrementScore(
                    room.getRoomCode(),
                    player.getId(),
                    points
            );
        }
    }

    public void advanceQuestionAutomatically(Room room, Quiz quiz) {
        
        int index = redisGameStateService.getCurrentQuestionIndex(room.getRoomCode());
        checkIfLastQuestion(room, quiz, index);
        redisGameStateService.setCurrentQuestionIndex(room.getRoomCode(), index + 1);

        startQuestionTimer(room, quiz);
    }

    public void checkIfLastQuestion(Room room, Quiz quiz,int index) {
        
        if (index + 1 >= quiz.getQuestions().size()) {
            finishGame(room.getRoomCode());
            return;
        }
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
