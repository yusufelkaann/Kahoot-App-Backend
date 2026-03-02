package com.kahoot_app.Kahoot_App.quiz.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.quiz.dtos.AnswerOptionDTO;
import com.kahoot_app.Kahoot_App.quiz.dtos.QuestionDTO;
import com.kahoot_app.Kahoot_App.quiz.dtos.QuestionResponseDTO;
import com.kahoot_app.Kahoot_App.quiz.dtos.QuizRequestDTO;
import com.kahoot_app.Kahoot_App.quiz.dtos.QuizResponseDTO;
import com.kahoot_app.Kahoot_App.quiz.entities.AnswerOption;
import com.kahoot_app.Kahoot_App.quiz.entities.Question;
import com.kahoot_app.Kahoot_App.quiz.entities.Quiz;
import com.kahoot_app.Kahoot_App.quiz.mappers.QuizMapper;
import com.kahoot_app.Kahoot_App.quiz.repository.QuizRepository;

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
                .orElseThrow(() -> new RuntimeException("Quiz not found with id" + id));
        
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
                throw new IllegalArgumentException(
                        "Each question must have exactly one correct answer"
                );
            }

            if (question.points() == null || question.points() <= 0) {
                throw new IllegalArgumentException(
                        "Question points must be greater than 0"
                );
            }

            if (question.timeLimitSeconds() == null || question.timeLimitSeconds() <= 0) {
                throw new IllegalArgumentException(
                        "Time limit must be greater than 0"
                );
            }
        });
    }
}
