package com.kahoot_app.Kahoot_App.entity;

import java.time.LocalDateTime;

import com.kahoot_app.Kahoot_App.enums.PlayerRole;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private int score = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column
    private PlayerRole role;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    public void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
        if (this.role == null) {
            this.role = PlayerRole.PLAYER;
        }
    }

    public Player(String nickname, Room room, PlayerRole role) {
        this.nickname = nickname;
        this.room = room;
        this.score = 0;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }
}
