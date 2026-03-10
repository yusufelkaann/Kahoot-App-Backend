package com.kahoot_app.Kahoot_App.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kahoot_app.Kahoot_App.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
