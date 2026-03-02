package com.kahoot_app.Kahoot_App.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kahoot_app.Kahoot_App.quiz.entities.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
