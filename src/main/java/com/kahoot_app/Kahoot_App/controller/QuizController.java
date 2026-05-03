package com.kahoot_app.Kahoot_App.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.kahoot_app.Kahoot_App.dto.GenerateQuizRequestDTO;
import com.kahoot_app.Kahoot_App.dto.QuizRequestDTO;
import com.kahoot_app.Kahoot_App.dto.QuizResponseDTO;
import com.kahoot_app.Kahoot_App.service.AiQuizService;
import com.kahoot_app.Kahoot_App.service.QuizService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;



@RestController
@RequestMapping("/api/v1/quizzes")
@Tag(name = "Quiz Controller", description = "APIs for managing quizzes")
public class QuizController {

    private final QuizService quizService;
    private final AiQuizService aiQuizService;

    public QuizController(QuizService quizService, AiQuizService aiQuizService) {
        this.quizService = quizService;
        this.aiQuizService = aiQuizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new quiz", description = "Creates a new quiz with questions and answer options")
    public QuizResponseDTO createQuiz(@Valid @RequestBody QuizRequestDTO request) {
        return quizService.createQuiz(request);
    }

    @GetMapping
    @Operation(summary = "Get all quizzes", description = "Retrieves a list of all available quizzes")
    public List<QuizResponseDTO> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID", description = "Retrieves a specific quiz by its ID")
    public QuizResponseDTO getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id);
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate quiz with AI", description = "Generates a new quiz using AI based on the provided topic")
    public QuizResponseDTO generateQuiz(@Valid @RequestBody GenerateQuizRequestDTO request) {
        return aiQuizService.generateQuiz(request);
    }

    // Delete endpoint
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete quiz", description = "Deletes a quiz by its ID")
    public void deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
    }
}
