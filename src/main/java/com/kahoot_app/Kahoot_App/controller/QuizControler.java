package com.kahoot_app.Kahoot_App.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kahoot_app.Kahoot_App.dto.QuizRequestDTO;
import com.kahoot_app.Kahoot_App.dto.QuizResponseDTO;
import com.kahoot_app.Kahoot_App.service.QuizService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;



@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizControler {

    private final QuizService quizService;

    public QuizControler(QuizService quizService) {
        this.quizService = quizService;
    }

    // Create quiz
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponseDTO createQuiz(@Valid @RequestBody QuizRequestDTO request) {
        return quizService.createQuiz(request);
    }

    // Get all quizzes
    @GetMapping
    public List<QuizResponseDTO> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }
    
    // Get quiz by id
    @GetMapping("/{id}")
    public QuizResponseDTO getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id);
    }
}
