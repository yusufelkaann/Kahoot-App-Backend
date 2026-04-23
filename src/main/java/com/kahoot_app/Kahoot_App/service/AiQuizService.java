package com.kahoot_app.Kahoot_App.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.kahoot_app.Kahoot_App.dto.GenerateQuizRequestDTO;
import com.kahoot_app.Kahoot_App.dto.QuizRequestDTO;
import com.kahoot_app.Kahoot_App.dto.QuizResponseDTO;

@Service
public class AiQuizService {

    private final ChatClient chatClient;
    private final QuizService quizService;

    public AiQuizService(ChatClient.Builder chatClientBuilder, QuizService quizService) {
        this.chatClient = chatClientBuilder.build();
        this.quizService = quizService;
    }

    public QuizResponseDTO generateQuiz(GenerateQuizRequestDTO request) {
        QuizRequestDTO quizRequest = chatClient.prompt()
                .user(buildPrompt(request))
                .call()
                .entity(QuizRequestDTO.class);

        return quizService.createQuiz(quizRequest);
    }

    private String buildPrompt(GenerateQuizRequestDTO request) {
        return """
                Generate a %s difficulty quiz about "%s" with exactly %d multiple-choice questions.
                Rules:
                - Each question must have exactly 4 answer options
                - Exactly 1 option per question must have isCorrect: true, the rest false
                - timeLimitSeconds: 30 (easy), 20 (medium), 15 (hard)
                - points: 100 (easy), 200 (medium), 300 (hard)
                - orderIndex starts at 1

                Return ONLY a JSON object with this structure, no extra text:
                {
                  "title": "<quiz title>",
                  "description": "<short description>",
                  "questions": [
                    {
                      "questionText": "<question>",
                      "timeLimitSeconds": <number>,
                      "points": <number>,
                      "orderIndex": <number>,
                      "options": [
                        { "text": "<answer>", "isCorrect": true },
                        { "text": "<answer>", "isCorrect": false },
                        { "text": "<answer>", "isCorrect": false },
                        { "text": "<answer>", "isCorrect": false }
                      ]
                    }
                  ]
                }
                """.formatted(request.difficulty(), request.topic(), request.questionCount());
    }
}
