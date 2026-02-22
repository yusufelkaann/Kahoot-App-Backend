package com.kahoot_app.Kahoot_App.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kahoot_app.Kahoot_App.dtos.AnswerOptionDTO;
import com.kahoot_app.Kahoot_App.dtos.QuestionDTO;
import com.kahoot_app.Kahoot_App.dtos.QuizRequestDTO;
import com.kahoot_app.Kahoot_App.dtos.QuizResponseDTO;
import com.kahoot_app.Kahoot_App.entities.AnswerOption;
import com.kahoot_app.Kahoot_App.entities.Question;
import com.kahoot_app.Kahoot_App.entities.Quiz;
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
        
        validateRequest(request);

        Quiz quiz = new Quiz();
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());


        // Create questions
        request.questions().forEach(questionDTO -> {
            Question question = new Question();
            question.setQuestionText(questionDTO.questionText());
            question.setTimeLimitSeconds(questionDTO.timeLimitSeconds());
            question.setPoints(questionDTO.points());
            question.setOrderIndex(questionDTO.orderIndex());

            // Create answer options
            questionDTO.answerOptions().forEach(optionDTO -> {
                AnswerOption option = new AnswerOption();
                option.setText(optionDTO.text());
                option.setIsCorrect(optionDTO.isCorrect());
                question.addOption(option);
            });

            quiz.addQuestion(question);
        });
        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToResponse(savedQuiz);
    }

    // Get all quizzes
    @Transactional(readOnly = true)
    public List<QuizResponseDTO> getAllQuizzes() {
        return quizRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get Quiz by id
    @Transactional(readOnly = true)
    public QuizResponseDTO getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id" + id));
        
        return mapToResponse(quiz);
    }


    // Validate Request
    /*
        check title
        check questions
        check answer options
        check correct count
    */
    private void validateRequest(QuizRequestDTO request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Quiz title cannot be empty");
        }

        if (request.questions() == null || request.questions().isEmpty()) {
            throw new IllegalArgumentException("Quiz must contain at least one question");
        }

        request.questions().forEach(question -> {

            if (question.answerOptions() == null || question.answerOptions().isEmpty()) {
                throw new IllegalArgumentException("Each question must have at least one answer option");
            }

            long correctCount = question.answerOptions()
                    .stream()
                    .filter(option -> Boolean.TRUE.equals(option.isCorrect()))
                    .count();

            if (correctCount != 1) {
                throw new IllegalArgumentException("Each question must have exactly one correct answer");
            }
        });
    }

    // enttity to dto mapping
    private QuizResponseDTO mapToResponse(Quiz quiz) {
        List<QuestionDTO> questionDTOs = quiz.getQuestions()
            .stream()
            .sorted((q1, q2) -> q1.getOrderIndex().compareTo(q2.getOrderIndex()))
            .map(question -> new QuestionDTO(
                question.getQuestionText(),
                question.getTimeLimitSeconds(),
                question.getPoints(),
                question.getOrderIndex(),
                question.getOptions()
                        .stream()
                        .map(option -> new AnswerOptionDTO(
                                option.getText(),
                                option.getIsCorrect()
                        )).toList()
            ))
            .toList();
        
        return new QuizResponseDTO(
            quiz.getId(),
            quiz.getTitle(),
            quiz.getDescription(),
            quiz.getCreatedAt(),
            questionDTOs
        );
    }
}
