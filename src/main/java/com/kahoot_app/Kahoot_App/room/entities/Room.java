package com.kahoot_app.Kahoot_App.room.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kahoot_app.Kahoot_App.quiz.entities.Quiz;
import com.kahoot_app.Kahoot_App.room.enums.RoomStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private Integer currentQuestionIndex;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    public Room(String roomCode, Quiz quiz) {
        this.roomCode = roomCode;
        this.quiz = quiz;
        this.status = RoomStatus.WAITING;
        this.currentQuestionIndex = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public UUID getId() { return id; }

    public String getRoomCode() { return roomCode; }

    public RoomStatus getStatus() { return status; }

    public void setStatus(RoomStatus status) { this.status = status; }

    public Quiz getQuiz() { return quiz; }

    public Integer getCurrentQuestionIndex() { return currentQuestionIndex; }

    public void setCurrentQuestionIndex(Integer currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
}
