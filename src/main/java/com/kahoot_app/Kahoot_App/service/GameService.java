package com.kahoot_app.Kahoot_App.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.*;
import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.dto.LeaderBoardEntryDTO;
import com.kahoot_app.Kahoot_App.entity.AnswerOption;
import com.kahoot_app.Kahoot_App.entity.Player;
import com.kahoot_app.Kahoot_App.entity.Question;
import com.kahoot_app.Kahoot_App.entity.Quiz;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.enums.PlayerRole;
import com.kahoot_app.Kahoot_App.enums.RoomStatus;
import com.kahoot_app.Kahoot_App.global.exceptions.*;
import com.kahoot_app.Kahoot_App.repository.PlayerRepository;
import com.kahoot_app.Kahoot_App.repository.QuizRepository;
import com.kahoot_app.Kahoot_App.repository.RoomRepository;

@Service
public class GameService {
    private final RoomRepository roomRepository;
    private final QuizRepository quizRepository;
    private final PlayerRepository playerRepository;

    private final RedisGameStateService redisGameStateService;
    private final GameTimerService gameTimerService;
    private final GameWebSocketService webSocketService;

    private final Random random = new Random();

    public GameService(RoomRepository roomRepository, 
        QuizRepository quizRepository,
        PlayerRepository playerRepository,
        RedisGameStateService redisGameStateService,
        GameTimerService gameTimerService,
        GameWebSocketService webSocketService) {
        this.roomRepository = roomRepository;
        this.quizRepository = quizRepository;
        this.playerRepository = playerRepository;
        this.redisGameStateService = redisGameStateService;
        this.gameTimerService = gameTimerService;
        this.webSocketService = webSocketService;
    }


    // Create empty room
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

        // Get next question's time limit
        Question firstQuestion = room.getQuiz().getQuestions().get(0);
        int timeLimitSeconds = firstQuestion.getTimeLimitSeconds();

        // Generate timer token to prevent race conditions
        String timerToken = redisGameStateService.generateTimerToken(roomCode, 0);
        gameTimerService.startQuestionTimer(roomCode, 0, timerToken,timeLimitSeconds);

        room.setCurrentQuestionIndex(0);
        webSocketService.broadcastQuestionAdvance(roomCode, 0);
    }

    /*
     Finishes the game and persists the FINISHED status.
     */
    @Transactional
    public void finishGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getStatus() != RoomStatus.STARTED) {
            throw new ConflictException("Game not started");
        }

        syncScoresToDatabase(room);
        
        // Set status
        room.setStatus(RoomStatus.FINISHED);
        redisGameStateService.setGameStatus(roomCode, RoomStatus.FINISHED);

        // Broadcast game finished
        webSocketService.broadcastGameFinished(roomCode);
        
        redisGameStateService.clearRoom(roomCode);
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
        Player savedPlayer = playerRepository.save(player);
        redisGameStateService.initializeScore(roomCode, savedPlayer.getId());

        return savedPlayer;
    }

    @Transactional
    public void submitAnswer(String roomCode, Long playerId, Long answerOptionID) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        RoomStatus status = redisGameStateService.getGameStatus(roomCode);
        Player player = room.getPlayers().stream()
            .filter(p -> p.getId().equals(playerId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Player not found"));
        
        if (status != RoomStatus.STARTED) {
            throw new BadRequestException("Game has not started yet");
        }

        if (player.getRole() == PlayerRole.HOST) {
            throw new BadRequestException("Host cannot answer questions");
        }

        int currentQuestion = redisGameStateService.getCurrentQuestionIndexSafe(room.getRoomCode());
        Question question = room.getQuiz().getQuestions().get(currentQuestion);

        if (redisGameStateService.hasAnswered(roomCode, currentQuestion, playerId)) {
            throw new BadRequestException("Player already answered this question");
        }

        redisGameStateService.saveAnswer(roomCode, currentQuestion, playerId, answerOptionID);

        calculateAndApplyScore(room, player, question, answerOptionID);
    }

    @Transactional 
    public void advanceQuestionManually(String roomCode, long playerId) {

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        Player player = room.getPlayers().stream()
            .filter(p -> p.getId().equals(playerId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Player not found"));
        Quiz quiz = room.getQuiz();
        
        if (player.getRole() != PlayerRole.HOST) {
            throw new BadRequestException("Player cannot change the question");
        }

        // Use safe method to handle null from Redis
        int index = redisGameStateService.getCurrentQuestionIndexSafe(roomCode);
        if (isLastQuestion(room, quiz, index)) {
            finishGame(roomCode);
            return;
        };

        int nextIndex = index + 1;
        // Get next question's time limit
        Question nextQuestion = quiz.getQuestions().get(nextIndex);
        int timeLimitSeconds = nextQuestion.getTimeLimitSeconds();
        redisGameStateService.setCurrentQuestionIndex(roomCode, nextIndex);

        webSocketService.broadcastQuestionAdvance(roomCode, nextIndex);
        
        // Generate new timer token to invalidate any running timer
        String timerToken = redisGameStateService.generateTimerToken(roomCode, nextIndex);
        gameTimerService.startQuestionTimer(roomCode, nextIndex, timerToken, timeLimitSeconds);
    }

    @Transactional
    public void advanceQuestionAutomaticallyByRoomCode(String roomCode) {
        Room room = getRoomByCode(roomCode);
        
        // Check if game is still running
        if (room.getStatus() != RoomStatus.STARTED) {
            return; // Game ended, don't advance
        }
        
        Quiz quiz = room.getQuiz();
        if (quiz == null) {
            return; // No quiz assigned
        }
        
        
        int index = redisGameStateService.getCurrentQuestionIndexSafe(roomCode);
        
        if (isLastQuestion(room, quiz, index)) {
            finishGame(room.getRoomCode());
            return;
        };
        
        int nextIndex = index + 1;
        // Get next question's time limit
        Question nextQuestion = quiz.getQuestions().get(nextIndex);
        int timeLimitSeconds = nextQuestion.getTimeLimitSeconds();
        redisGameStateService.setCurrentQuestionIndex(roomCode, nextIndex);

        // Broadcast question advace
        webSocketService.broadcastQuestionAdvance(roomCode, nextIndex);
        
        // Generate new timer token for the next question
        String timerToken = redisGameStateService.generateTimerToken(roomCode, nextIndex);
        gameTimerService.startQuestionTimer(roomCode,nextIndex, timerToken, timeLimitSeconds);
    }

    @Transactional(readOnly = true)
    public List<LeaderBoardEntryDTO> getLeaderboard(String roomCode) {
        Room room = getRoomByCode(roomCode);

        List<Player> sortedPlayers = room.getPlayers().stream()
            .filter(p -> p.getRole() == PlayerRole.PLAYER)
            .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
            .toList();

        return IntStream.range(0, sortedPlayers.size())
            .mapToObj(i -> {
                Player player = sortedPlayers.get(i);
                return new LeaderBoardEntryDTO(
                    player.getId(),
                    player.getNickname(),
                    redisGameStateService.getScore(roomCode, player.getId()),
                    i + 1
                );
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public int getCurrentQuestionIndex(String roomCode) {

        Room room = getRoomByCode(roomCode);
    
        if (room.getStatus() != RoomStatus.STARTED) {
            throw new BadRequestException("Game not started");
        }
        return redisGameStateService.getCurrentQuestionIndexSafe(roomCode);
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

            // Update score in Redis for real-time tracking
            redisGameStateService.incrementScore(
                    room.getRoomCode(),
                    player.getId(),
                    points
            );

            webSocketService.broadcastScoreUpdate(room.getRoomCode(), player.getId(),player.getScore());
        }
    }

    public boolean isLastQuestion(Room room, Quiz quiz, int index) {
        
        if (index + 1 >= quiz.getQuestions().size()) {
            return true;
        }

        return false;
    }

    private void syncScoresToDatabase(Room room) {
        for (Player player : room.getPlayers()) {
            if (player.getRole() == PlayerRole.PLAYER) {
                int finalScore = redisGameStateService.getScore(room.getRoomCode(), player.getId());
                player.setScore(finalScore);
                playerRepository.save(player);
            }
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
