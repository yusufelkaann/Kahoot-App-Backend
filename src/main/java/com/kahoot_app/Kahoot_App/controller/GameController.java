package com.kahoot_app.Kahoot_App.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kahoot_app.Kahoot_App.dto.JoinRoomRequestDTO;
import com.kahoot_app.Kahoot_App.dto.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.mappers.RoomMapper;
import com.kahoot_app.Kahoot_App.service.GameService;
import com.kahoot_app.Kahoot_App.service.RedisGameStateService;
import com.kahoot_app.Kahoot_App.dto.LeaderBoardEntryDTO;
import com.kahoot_app.Kahoot_App.dto.QuestionDTO;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;





@RestController
@RequestMapping("/api/v1/rooms")
public class GameController {
    
    private final GameService gameService;
    private final RedisGameStateService redisGameStateService;

    public GameController(GameService gameService, RedisGameStateService redisGameStateService) {
        this.gameService = gameService;
        this.redisGameStateService = redisGameStateService;
    }

    // create empty room
    @PostMapping
    public RoomResponseDTO createRoom() {
        Room room = gameService.createRoom();
        return RoomMapper.toRoomResponseDTO(room, redisGameStateService);
    }

    // join room
    @PostMapping("/{roomCode}/join")
    public RoomResponseDTO joinRoom(
            @PathVariable String roomCode,
            @Valid @RequestBody JoinRoomRequestDTO request
    ) {
        gameService.joinRoom(roomCode, request.nickname());
        Room room = gameService.getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room, redisGameStateService);
    } 

    @PostMapping("/{roomCode}/assign-quiz/{quizId}")
    public RoomResponseDTO assignQuiz(
            @PathVariable String roomCode,
            @PathVariable Long quizId
    ) {
        Room room = gameService.assignQuiz(roomCode, quizId);
        return RoomMapper.toRoomResponseDTO(room, redisGameStateService);
    }

    @PostMapping("/{roomCode}/start")
    public RoomResponseDTO startGame(@PathVariable String roomCode) {
        gameService.startGame(roomCode);
        Room room = gameService.getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room, redisGameStateService);
    }

    @PostMapping("/{roomCode}/finish")
    public RoomResponseDTO finishGame(@PathVariable String roomCode) {
        gameService.finishGame(roomCode);
        Room room = gameService.getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room, redisGameStateService);
    }

    @GetMapping("/{roomCode}")
    public RoomResponseDTO getRoom(@PathVariable String roomCode) {
        Room room = gameService.getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room, redisGameStateService);
    }

    @GetMapping("/{roomCode}/leaderboard")
    public ResponseEntity<List<LeaderBoardEntryDTO>> getLeaderboard(@PathVariable String roomCode) {
        List<LeaderBoardEntryDTO> leaderboard = gameService.getLeaderboard(roomCode);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/{roomCode}/time-remaining")
    public ResponseEntity<Map<String, Long>> getTimeRemaining(@PathVariable String roomCode) {
        long seconds = redisGameStateService.getTimerTTL(roomCode);
        return ResponseEntity.ok(Map.of("secondsRemaining", seconds));
    }

    


    

}
