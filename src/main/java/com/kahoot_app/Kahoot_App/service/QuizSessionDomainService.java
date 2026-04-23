package com.kahoot_app.Kahoot_App.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.entity.Room;
import com.kahoot_app.Kahoot_App.enums.RoomStatus;
import com.kahoot_app.Kahoot_App.repository.RoomRepository;

@Service
@Transactional(readOnly = true)
public class QuizSessionDomainService {

    private final RoomRepository roomRepository;

    public QuizSessionDomainService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public boolean isQuizActiveInSession(Long quizId) {
        return roomRepository.existsByQuizIdAndStatus(quizId, RoomStatus.STARTED);
    }

    public Optional<Room> getActiveSessionForQuiz(Long quizId) {
        return roomRepository.findByQuizIdAndStatus(quizId, RoomStatus.STARTED);
    }
}
