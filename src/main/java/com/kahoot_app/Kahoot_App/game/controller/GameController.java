package com.kahoot_app.Kahoot_App.game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kahoot_app.Kahoot_App.game.service.GameService;
import com.kahoot_app.Kahoot_App.redis.service.RedisGameStateService;
import com.kahoot_app.Kahoot_App.room.dtos.JoinRoomRequestDTO;
import com.kahoot_app.Kahoot_App.room.dtos.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.room.entities.Room;
import com.kahoot_app.Kahoot_App.room.mappers.RoomMapper;

import jakarta.validation.Valid;
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

}
