package com.kahoot_app.Kahoot_App.game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kahoot_app.Kahoot_App.game.service.GameService;
import com.kahoot_app.Kahoot_App.room.dtos.JoinRoomRequestDTO;
import com.kahoot_app.Kahoot_App.room.dtos.RoomResponseDTO;
import com.kahoot_app.Kahoot_App.room.entities.Room;
import com.kahoot_app.Kahoot_App.room.mappers.RoomMapper;

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
    public RoomResponseDTO createRoom() {
        Room room = gameService.createRoom();
        return RoomMapper.toRoomResponseDTO(room);
    }

    // join room
    @PostMapping("/{roomCode}/join")
    public RoomResponseDTO joinRoom(
            @PathVariable String roomCode,
            @RequestBody JoinRoomRequestDTO request
    ) {
        gameService.joinRoom(roomCode, request.nickname());
        Room room = gameService.getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room);
    } 

    @PostMapping("/{roomCode}/assign-quiz/{quizId}")
    public RoomResponseDTO assignQuiz(
            @PathVariable String roomCode,
            @PathVariable Long quizId
    ) {
        Room room = gameService.assignQuiz(roomCode, quizId);
        return RoomMapper.toRoomResponseDTO(room);
    }

    @PostMapping("/{roomCode}/start")
    public RoomResponseDTO startGame(@PathVariable String roomCode) {
        gameService.startGame(roomCode);
        Room room = gameService.getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room);
    }

    @GetMapping("/{roomCode}")
    public RoomResponseDTO getRoom(@PathVariable String roomCode) {
        Room room = gameService.getRoomByCode(roomCode);
        return RoomMapper.toRoomResponseDTO(room);
    }

}
