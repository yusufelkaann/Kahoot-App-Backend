package com.kahoot_app.Kahoot_App.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.kahoot_app.Kahoot_App.dto.CreateRoomRequestDTO;
import com.kahoot_app.Kahoot_App.dto.JoinRoomRequestDTO;
import com.kahoot_app.Kahoot_App.dto.LeaderBoardEntryDTO;
import com.kahoot_app.Kahoot_App.dto.QuestionDTO;
import com.kahoot_app.Kahoot_App.dto.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.dto.SubmitAnswerResponseDTO;
import com.kahoot_app.Kahoot_App.entity.Question;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.mappers.AnswerMapper;
import com.kahoot_app.Kahoot_App.mappers.QuestionMapper;
import com.kahoot_app.Kahoot_App.command.GameCommandFactory;
import com.kahoot_app.Kahoot_App.service.GameFlowService;
import com.kahoot_app.Kahoot_App.service.RoomService;
import com.kahoot_app.Kahoot_App.service.ScoringService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Game Controller", description = "APIs for managing game rooms and gameplay")
public class GameController {

    private final RoomService roomService;
    private final GameFlowService gameFlowService;
    private final ScoringService scoringService;
    private final GameCommandFactory gameCommandFactory;

    public GameController(RoomService roomService,
            GameFlowService gameFlowService,
            ScoringService scoringService,
            GameCommandFactory gameCommandFactory) {
        this.roomService = roomService;
        this.gameFlowService = gameFlowService;
        this.scoringService = scoringService;
        this.gameCommandFactory = gameCommandFactory;
    }

    @PostMapping
    @Operation(summary = "Create a new game room", description = "Creates a new game room with the specified host nickname")
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody CreateRoomRequestDTO request) {
        Room room = roomService.createRoom(request.hostNickname());
        RoomResponseDTO response = roomService.getRoomResponseDTO(room.getRoomCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{roomCode}/join")
    @Operation(summary = "Join a game room", description = "Allows a player to join an existing game room")
    public RoomResponseDTO joinRoom(
            @PathVariable String roomCode,
            @Valid @RequestBody JoinRoomRequestDTO request) {
        roomService.joinRoom(roomCode, request.nickname());
        return roomService.getRoomResponseDTO(roomCode);
    }

    @PostMapping("/{roomCode}/assign-quiz/{quizId}")
    @Operation(summary = "Assign quiz to room", description = "Assigns a quiz to a game room")
    public RoomResponseDTO assignQuiz(
            @PathVariable String roomCode,
            @PathVariable Long quizId) {
        roomService.assignQuiz(roomCode, quizId);
        return roomService.getRoomResponseDTO(roomCode);
    }

    @PostMapping("/{roomCode}/start")
    @Operation(summary = "Start game", description = "Starts the game in the specified room")
    public RoomResponseDTO startGame(@PathVariable String roomCode) {
        gameCommandFactory.startGame(roomCode).execute();
        return roomService.getRoomResponseDTO(roomCode);
    }

    @PostMapping("/{roomCode}/submit-answer")
    @Operation(summary = "Submit answer", description = "Submits a player's answer to the current question")
    public SubmitAnswerResponseDTO submitAnswer(
            @PathVariable String roomCode,
            @RequestParam Long playerId,
            @RequestParam Long answerOptionId) {
        int currentScore = scoringService.submitAnswer(roomCode, playerId, answerOptionId);
        return AnswerMapper.toSubmitAnswerResponse(playerId, currentScore);
    }

    @PostMapping("/{roomCode}/advance")
    @Operation(summary = "Advance to next question", description = "Moves the game to the next question")
    public ResponseEntity<?> advanceQuestion(
            @PathVariable String roomCode,
            @RequestParam Long hostPlayerId) {
        gameCommandFactory.advanceQuestion(roomCode, hostPlayerId).execute();
        return ResponseEntity.ok(roomService.getRoomResponseDTO(roomCode));
    }

    @PostMapping("/{roomCode}/finish")
    @Operation(summary = "Finish game", description = "Ends the game and finalizes results")
    public RoomResponseDTO finishGame(@PathVariable String roomCode) {
        gameCommandFactory.finishGame(roomCode).execute();
        return roomService.getRoomResponseDTO(roomCode);
    }

    @GetMapping("/{roomCode}")
    @Operation(summary = "Get room details", description = "Retrieves the details of a specific game room")
    public RoomResponseDTO getRoom(@PathVariable String roomCode) {
        return roomService.getRoomResponseDTO(roomCode);
    }

    @GetMapping("/{roomCode}/current-question")
    @Operation(summary = "Get current question", description = "Retrieves the current question for the specified room")
    public ResponseEntity<QuestionDTO> getCurrentQuestion(@PathVariable String roomCode) {
        int index = gameFlowService.getCurrentQuestionIndex(roomCode);
        Room room = gameFlowService.getRoomByCode(roomCode);
        Question question = room.getQuiz().getQuestions().get(index);
        QuestionDTO dto = QuestionMapper.toQuestionDTO(question, true);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{roomCode}/leaderboard")
    @Operation(summary = "Get leaderboard", description = "Retrieves the leaderboard for the specified room")
    public ResponseEntity<List<LeaderBoardEntryDTO>> getLeaderboard(@PathVariable String roomCode) {
        List<LeaderBoardEntryDTO> leaderboard = scoringService.getLeaderboard(roomCode);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/{roomCode}/time-remaining")
    @Operation(summary = "Get time remaining", description = "Retrieves the remaining time for the current question")
    public ResponseEntity<Map<String, Long>> getTimeRemaining(@PathVariable String roomCode) {
        long seconds = gameFlowService.getTimeRemaining(roomCode);
        return ResponseEntity.ok(Map.of("secondsRemaining", seconds));
    }
}
