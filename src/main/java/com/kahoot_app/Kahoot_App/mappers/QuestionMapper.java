package com.kahoot_app.Kahoot_App.mappers;

import com.kahoot_app.Kahoot_App.dto.AnswerOptionDTO;
import com.kahoot_app.Kahoot_App.dto.QuestionDTO;
import com.kahoot_app.Kahoot_App.entity.Question;

public class QuestionMapper {
    private QuestionMapper() {
    
    }

    public static QuestionDTO toQuestionDTO(Question question, boolean hideCorrectAnswer) {
        return new QuestionDTO(
            question.getId(),
            question.getQuestionText(),
            question.getTimeLimitSeconds(),
            question.getPoints(),
            question.getOrderIndex(),
            question.getOptions().stream()
                .map(opt -> new AnswerOptionDTO(
                    opt.getId(),
                    opt.getText(),
                    hideCorrectAnswer ? null : opt.getIsCorrect()
                ))
                .toList()
        );
    }

    public static QuestionDTO toDTOForPlayers(Question question) {
        return toQuestionDTO(question, true);
    }

    public static QuestionDTO toDTO(Question question) {
        return toQuestionDTO(question, false);
    }
}
