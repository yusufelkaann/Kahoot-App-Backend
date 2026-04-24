package com.kahoot_app.Kahoot_App.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.dto.LeaderBoardEntryDTO;
import com.kahoot_app.Kahoot_App.entity.AnswerOption;
import com.kahoot_app.Kahoot_App.entity.Player;
import com.kahoot_app.Kahoot_App.entity.Question;
import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.enums.PlayerRole;
import com.kahoot_app.Kahoot_App.enums.RoomStatus;
import com.kahoot_app.Kahoot_App.global.exceptions.BadRequestException;
import com.kahoot_app.Kahoot_App.global.exceptions.NotFoundException;
import com.kahoot_app.Kahoot_App.repository.AnswerStore;
import com.kahoot_app.Kahoot_App.repository.GameStatusStore;
import com.kahoot_app.Kahoot_App.repository.PlayerRepository;
import com.kahoot_app.Kahoot_App.repository.RoomRepository;
import com.kahoot_app.Kahoot_App.repository.ScoreStore;

@Service
public class ScoringService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final GameStatusStore gameStatusStore;
    private final ScoreStore scoreStore;
    private final AnswerStore answerStore;
    private final GameWebSocketService webSocketService;

    public ScoringService(RoomRepository roomRepository,
            PlayerRepository playerRepository,
            GameStatusStore gameStatusStore,
            ScoreStore scoreStore,
            AnswerStore answerStore,
            GameWebSocketService webSocketService) {
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
        this.gameStatusStore = gameStatusStore;
        this.scoreStore = scoreStore;
        this.answerStore = answerStore;
        this.webSocketService = webSocketService;
    }

    @Transactional
    public int submitAnswer(String roomCode, Long playerId, Long answerOptionId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        RoomStatus status = gameStatusStore.getGameStatus(roomCode);
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

        int currentQuestion = gameStatusStore.getCurrentQuestionIndexSafe(roomCode);
        Question question = room.getQuiz().getQuestions().get(currentQuestion);

        if (answerStore.hasAnswered(roomCode, currentQuestion, playerId)) {
            throw new BadRequestException("Player already answered this question");
        }

        answerStore.saveAnswer(roomCode, currentQuestion, playerId, answerOptionId);
        applyScore(room, player, question, answerOptionId);

        return scoreStore.getScore(roomCode, playerId);
    }

    @Transactional(readOnly = true)
    public List<LeaderBoardEntryDTO> getLeaderboard(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        RoomStatus status = gameStatusStore.getGameStatus(roomCode);

        List<Player> players = room.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerRole.PLAYER)
                .toList();

        if (status == RoomStatus.FINISHED || status == null) {
            List<Player> sorted = players.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
                    .toList();
            return IntStream.range(0, sorted.size())
                    .mapToObj(i -> new LeaderBoardEntryDTO(
                            sorted.get(i).getId(),
                            sorted.get(i).getNickname(),
                            sorted.get(i).getScore(),
                            i + 1))
                    .toList();
        }

        List<LeaderBoardEntryDTO> leaderboard = players.stream()
                .map(player -> new LeaderBoardEntryDTO(
                        player.getId(),
                        player.getNickname(),
                        scoreStore.getScore(roomCode, player.getId()),
                        0))
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .toList();

        return IntStream.range(0, leaderboard.size())
                .mapToObj(i -> new LeaderBoardEntryDTO(
                        leaderboard.get(i).playerId(),
                        leaderboard.get(i).nickname(),
                        leaderboard.get(i).score(),
                        i + 1))
                .toList();
    }

    void syncScoresToDatabase(Room room) {
        for (Player player : room.getPlayers()) {
            if (player.getRole() == PlayerRole.PLAYER) {
                int finalScore = scoreStore.getScore(room.getRoomCode(), player.getId());
                player.setScore(finalScore);
                playerRepository.save(player);
            }
        }
    }

    private void applyScore(Room room, Player player, Question question, Long optionId) {
        AnswerOption option = question.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Answer option not found"));

        if (option.getIsCorrect()) {
            scoreStore.incrementScore(room.getRoomCode(), player.getId(), question.getPoints());
            int updatedScore = scoreStore.getScore(room.getRoomCode(), player.getId());
            webSocketService.broadcastScoreUpdate(room.getRoomCode(), player.getId(), updatedScore);
        }
    }
}
