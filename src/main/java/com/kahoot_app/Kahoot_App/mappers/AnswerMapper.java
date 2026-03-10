package com.kahoot_app.Kahoot_App.mappers;

import com.kahoot_app.Kahoot_App.dto.SubmitAnswerResponseDTO;

public class AnswerMapper {
    private AnswerMapper() {
    
    }

    public static SubmitAnswerResponseDTO toSubmitAnswerResponse(
            Long playerId,
            Integer currentScore
    ) {
        return new SubmitAnswerResponseDTO(
            "Answer submitted successfully",
            playerId,
            currentScore
        );
    }
}
