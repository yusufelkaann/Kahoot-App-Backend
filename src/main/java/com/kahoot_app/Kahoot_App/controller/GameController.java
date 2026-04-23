package com.kahoot_app.Kahoot_App.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kahoot_app.Kahoot_App.dto.CreateRoomRequestDTO;
import com.kahoot_app.Kahoot_App.dto.JoinRoomRequestDTO;
import com.kahoot_app.Kahoot_App.dto.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.dto.SubmitAnswerResponseDTO;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.mappers.AnswerMapper;
import com.kahoot_app.Kahoot_App.mappers.QuestionMapper;
import com.kahoot_app.Kahoot_App.service.GameService;
import com.kahoot_app.Kahoot_App.dto.LeaderBoardEntryDTO;
import com.kahoot_app.Kahoot_App.dto.QuestionDTO;

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
public class GameController {
    
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // create empty room
    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody CreateRoomRequestDTO request) {
        Room room = gameService.createRoom(request.hostNickname());
        RoomResponseDTO response = gameService.getRoomResponseDTO(room.getRoomCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // join room
    @PostMapping("/{roomCode}/join")
    public RoomResponseDTO joinRoom(
            @PathVariable String roomCode,
            @Valid @RequestBody JoinRoomRequestDTO request
    ) {
        gameService.joinRoom(roomCode, request.nickname());
        return gameService.getRoomResponseDTO(roomCode);
    }

    @PostMapping("/{roomCode}/assign-quiz/{quizId}")
    public RoomResponseDTO assignQuiz(
            @PathVariable String roomCode,
            @PathVariable Long quizId
    ) {
        gameService.assignQuiz(roomCode, quizId);
        return gameService.getRoomResponseDTO(roomCode);
    }

    @PostMapping("/{roomCode}/start")
    public RoomResponseDTO startGame(@PathVariable String roomCode) {
        gameService.startGame(roomCode);
        return gameService.getRoomResponseDTO(roomCode);
    }

    @PostMapping("/{roomCode}/submit-answer")
    public SubmitAnswerResponseDTO submitAnswer(
            @PathVariable String roomCode,
            @RequestParam Long playerId,
            @RequestParam Long answerOptionId
    ) {
        int currentScore = gameService.submitAnswer(roomCode, playerId, answerOptionId);
        return AnswerMapper.toSubmitAnswerResponse(playerId, currentScore);
    }

    @PostMapping("/{roomCode}/advance")
    public ResponseEntity<?> advanceQuestion(
            @PathVariable String roomCode,
            @RequestParam Long hostPlayerId
    ) {
        gameService.advanceQuestionManually(roomCode, hostPlayerId);
        return ResponseEntity.ok(gameService.getRoomResponseDTO(roomCode));
    }

    @PostMapping("/{roomCode}/finish")
    public RoomResponseDTO finishGame(@PathVariable String roomCode) {
        gameService.finishGame(roomCode);
        return gameService.getRoomResponseDTO(roomCode);
    }

    @GetMapping("/{roomCode}")
    public RoomResponseDTO getRoom(@PathVariable String roomCode) {
        return gameService.getRoomResponseDTO(roomCode);
    }

    @GetMapping("/{roomCode}/current-question")
    public ResponseEntity<QuestionDTO> getCurrentQuestion(@PathVariable String roomCode) {
        
        int index = gameService.getCurrentQuestionIndex(roomCode);
        Room room = gameService.getRoomByCode(roomCode);
        Question question = room.getQuiz().getQuestions().get(index);
        
        // Don't expose which answer is correct to clients!
        QuestionDTO dto = QuestionMapper.toQuestionDTO(
            question,
            true
        );
        
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{roomCode}/leaderboard")
    public ResponseEntity<List<LeaderBoardEntryDTO>> getLeaderboard(@PathVariable String roomCode) {
        List<LeaderBoardEntryDTO> leaderboard = gameService.getLeaderboard(roomCode);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/{roomCode}/time-remaining")
    public ResponseEntity<Map<String, Long>> getTimeRemaining(@PathVariable String roomCode) {
        long seconds = gameService.getTimeRemaining(roomCode);
        return ResponseEntity.ok(Map.of("secondsRemaining", seconds));
    }

    


    

}
