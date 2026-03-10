package com.kahoot_app.Kahoot_App.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.dto.QuizRequestDTO;
import com.kahoot_app.Kahoot_App.dto.QuizResponseDTO;
import com.kahoot_app.Kahoot_App.entity.Quiz;
import com.kahoot_app.Kahoot_App.global.exceptions.*;
import com.kahoot_app.Kahoot_App.mappers.QuizMapper;
import com.kahoot_app.Kahoot_App.repository.QuizRepository;

@Service
@Transactional
public class QuizService {
    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    // Create Quiz
    public QuizResponseDTO createQuiz(QuizRequestDTO request) {
        
        validateBusinessRules(request);

        Quiz quiz = QuizMapper.toEntity(request);
        Quiz savedQuiz = quizRepository.save(quiz);
        return QuizMapper.toResponseDTO(savedQuiz);
    }

    // Get all quizzes
    @Transactional(readOnly = true)
    public List<QuizResponseDTO> getAllQuizzes() {
        return quizRepository.findAll()
                .stream()
                .map(QuizMapper::toResponseDTO)
                .toList();
    }

    // Get Quiz by id
    @Transactional(readOnly = true)
    public QuizResponseDTO getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Quiz not found with id" + id));
        
        return QuizMapper.toResponseDTO(quiz);
    }


    // Validate Request
    private void validateBusinessRules(QuizRequestDTO request) {

        request.questions().forEach(question -> {

            long correctCount = question.answerOptions()
                    .stream()
                    .filter(option -> Boolean.TRUE.equals(option.isCorrect()))
                    .count();

            if (correctCount != 1) {
                throw new BadRequestException(
                        "Each question must have exactly one correct answer"
                );
            }

            if (question.points() == null || question.points() <= 0) {
                throw new BadRequestException(
                        "Question points must be greater than 0"
                );
            }

            if (question.timeLimitSeconds() == null || question.timeLimitSeconds() <= 0) {
                throw new BadRequestException(
                        "Time limit must be greater than 0"
                );
            }
        });
    }
}
