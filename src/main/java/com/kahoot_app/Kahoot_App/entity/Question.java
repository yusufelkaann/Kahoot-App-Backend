package com.kahoot_app.Kahoot_App.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String questionText;

    @Column(nullable = false)
    private Integer timeLimitSeconds;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(
        mappedBy = "question",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<AnswerOption> options = new ArrayList<>();

    public void addOption(AnswerOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(AnswerOption option) {
        options.remove(option);
        option.setQuestion(null);
    }
}
