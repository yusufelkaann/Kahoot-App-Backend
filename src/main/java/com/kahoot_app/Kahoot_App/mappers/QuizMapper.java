package com.kahoot_app.Kahoot_App.mappers;

import java.util.List;
import java.util.stream.Collectors;

import com.kahoot_app.Kahoot_App.dtos.AnswerOptionDTO;
import com.kahoot_app.Kahoot_App.dtos.AnswerOptionResponseDTO;
import com.kahoot_app.Kahoot_App.dtos.QuestionDTO;
import com.kahoot_app.Kahoot_App.dtos.QuestionResponseDTO;
import com.kahoot_app.Kahoot_App.dtos.QuizRequestDTO;
import com.kahoot_app.Kahoot_App.dtos.QuizResponseDTO;
import com.kahoot_app.Kahoot_App.entities.AnswerOption;
import com.kahoot_app.Kahoot_App.entities.Question;
import com.kahoot_app.Kahoot_App.entities.Quiz;

public class QuizMapper {
    private QuizMapper() {
    }

    public static Quiz toEntity(QuizRequestDTO dto) {

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.title());
        quiz.setDescription(dto.description());

        List<Question> questions = dto.questions()
                .stream()
                .map(qDto -> {

                    Question question = new Question();
                    question.setQuestionText(qDto.questionText());
                    question.setOrderIndex(qDto.orderIndex());              // 🔥 IMPORTANT
                    question.setPoints(qDto.points());                      // 🔥 IMPORTANT
                    question.setTimeLimitSeconds(qDto.timeLimitSeconds());  // 🔥 IMPORTANT
                    question.setQuiz(quiz);

                    List<AnswerOption> options = qDto.answerOptions()
                            .stream()
                            .map(aDto -> {
                                AnswerOption option = new AnswerOption();
                                option.setText(aDto.text());
                                option.setIsCorrect(aDto.isCorrect());
                                option.setQuestion(question);
                                return option;
                            })
                            .toList();

                    question.setOptions(options);
                    return question;
                })
                .toList();

        quiz.setQuestions(questions);

        return quiz;
    }

    private static Question toQuestionEntity(QuestionDTO dto, Quiz quiz) {

        Question question = new Question();
        question.setQuestionText(dto.questionText());
        question.setQuiz(quiz);

        List<AnswerOption> options = dto.answerOptions()
                .stream()
                .map(o -> toAnswerOptionEntity(o, question))
                .collect(Collectors.toList());

        question.setOptions(options);

        return question;
    }

    private static AnswerOption toAnswerOptionEntity(
            AnswerOptionDTO dto,
            Question question
    ) {
        AnswerOption option = new AnswerOption();
        option.setText(dto.text());
        option.setIsCorrect(dto.isCorrect());
        option.setQuestion(question);
        return option;
    }

    // entity to dto
    public static QuizResponseDTO toResponseDTO(Quiz quiz) {

    List<QuestionResponseDTO> questions = quiz.getQuestions()
            .stream()
            .map(q -> new QuestionResponseDTO(
                    q.getId(),
                    q.getQuestionText(),
                    q.getTimeLimitSeconds(),
                    q.getPoints(),
                    q.getOrderIndex(),
                    q.getOptions()
                            .stream()
                            .map(o -> new AnswerOptionResponseDTO(
                                    o.getId(),
                                    o.getText()
                            ))
                            .toList()
            ))
            .toList();

    return new QuizResponseDTO(
            quiz.getId(),
            quiz.getTitle(),
            quiz.getDescription(),
            quiz.getCreatedAt(),
            questions
    );
}

}
