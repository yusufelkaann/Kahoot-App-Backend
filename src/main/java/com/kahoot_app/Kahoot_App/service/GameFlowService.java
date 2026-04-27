package com.kahoot_app.Kahoot_App.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.entity.Player;
import com.kahoot_app.Kahoot_App.entity.Question;
import com.kahoot_app.Kahoot_App.entity.Quiz;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.enums.PlayerRole;
import com.kahoot_app.Kahoot_App.enums.RoomStatus;
import com.kahoot_app.Kahoot_App.events.GameFinishedEvent;
import com.kahoot_app.Kahoot_App.events.QuestionAdvancedEvent;
import com.kahoot_app.Kahoot_App.global.exceptions.BadRequestException;
import com.kahoot_app.Kahoot_App.global.exceptions.NotFoundException;
import com.kahoot_app.Kahoot_App.repository.GameStatusStore;
import com.kahoot_app.Kahoot_App.repository.QuestionTimerStore;
import com.kahoot_app.Kahoot_App.repository.RoomRepository;
import com.kahoot_app.Kahoot_App.state.RoomStateFactory;

@Service
public class GameFlowService {

    private final RoomRepository roomRepository;
    private final GameStatusStore gameStatusStore;
    private final QuestionTimerStore questionTimerStore;
    private final ScoringService scoringService;
    private final ApplicationEventPublisher eventPublisher;

    public GameFlowService(RoomRepository roomRepository,
            GameStatusStore gameStatusStore,
            QuestionTimerStore questionTimerStore,
            ScoringService scoringService,
            ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.gameStatusStore = gameStatusStore;
        this.questionTimerStore = questionTimerStore;
        this.scoringService = scoringService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void startGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        RoomStateFactory.forStatus(room.getStatus()).onStart();

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
        gameStatusStore.setGameStatus(roomCode, RoomStatus.STARTED);
        gameStatusStore.setCurrentQuestionIndex(roomCode, 0);

        Question firstQuestion = room.getQuiz().getQuestions().get(0);
        int timeLimitSeconds = firstQuestion.getTimeLimitSeconds();

        String timerToken = questionTimerStore.generateTimerToken(roomCode, 0);
        room.setCurrentQuestionIndex(0);
        eventPublisher.publishEvent(new QuestionAdvancedEvent(roomCode, 0, timerToken, timeLimitSeconds));
    }

    @Transactional
    public void finishGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        RoomStateFactory.forStatus(room.getStatus()).onFinish();

        scoringService.syncScoresToDatabase(room);

        room.setStatus(RoomStatus.FINISHED);
        gameStatusStore.setGameStatus(roomCode, RoomStatus.FINISHED);

        eventPublisher.publishEvent(new GameFinishedEvent(roomCode));
        gameStatusStore.clearRoom(roomCode);
    }

    @Transactional
    public void advanceQuestionManually(String roomCode, long playerId) {
        Room room = getRoomByCode(roomCode);
        Player player = room.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Quiz quiz = room.getQuiz();

        RoomStateFactory.forStatus(room.getStatus()).onAdvance();

        if (player.getRole() != PlayerRole.HOST) {
            throw new BadRequestException("Player cannot change the question");
        }

        int index = gameStatusStore.getCurrentQuestionIndexSafe(roomCode);
        if (isLastQuestion(quiz, index)) {
            finishGame(roomCode);
            return;
        }

        int nextIndex = index + 1;
        Question nextQuestion = quiz.getQuestions().get(nextIndex);
        int timeLimitSeconds = nextQuestion.getTimeLimitSeconds();
        gameStatusStore.setCurrentQuestionIndex(roomCode, nextIndex);

        String timerToken = questionTimerStore.generateTimerToken(roomCode, nextIndex);
        eventPublisher.publishEvent(new QuestionAdvancedEvent(roomCode, nextIndex, timerToken, timeLimitSeconds));
    }

    @Transactional
    public void advanceQuestionAutomaticallyByRoomCode(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getStatus() != RoomStatus.STARTED) {
            return;
        }

        Quiz quiz = room.getQuiz();
        if (quiz == null) {
            return;
        }

        int index = gameStatusStore.getCurrentQuestionIndexSafe(roomCode);

        if (isLastQuestion(quiz, index)) {
            finishGame(roomCode);
            return;
        }

        int nextIndex = index + 1;
        Question nextQuestion = quiz.getQuestions().get(nextIndex);
        int timeLimitSeconds = nextQuestion.getTimeLimitSeconds();
        gameStatusStore.setCurrentQuestionIndex(roomCode, nextIndex);

        String timerToken = questionTimerStore.generateTimerToken(roomCode, nextIndex);
        eventPublisher.publishEvent(new QuestionAdvancedEvent(roomCode, nextIndex, timerToken, timeLimitSeconds));
    }

    @Transactional(readOnly = true)
    public int getCurrentQuestionIndex(String roomCode) {
        Room room = getRoomByCode(roomCode);
        if (room.getStatus() != RoomStatus.STARTED) {
            throw new BadRequestException("Game not started");
        }
        return gameStatusStore.getCurrentQuestionIndexSafe(roomCode);
    }

    @Transactional(readOnly = true)
    public long getTimeRemaining(String roomCode) {
        return questionTimerStore.getTimerTTL(roomCode);
    }

    public Room getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    private boolean isLastQuestion(Quiz quiz, int index) {
        return index + 1 >= quiz.getQuestions().size();
    }
}
