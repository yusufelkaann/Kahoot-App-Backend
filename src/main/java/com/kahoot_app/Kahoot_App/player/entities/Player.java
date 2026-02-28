package com.kahoot_app.Kahoot_App.player.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kahoot_app.Kahoot_App.room.entities.Room;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Integer score = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public Player() {}

    public Player(String nickname, Room room) {
        this.nickname = nickname;
        this.room = room;
        this.score = 0;
        this.joinedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }

    public String getNickname() { return nickname; }

    public Integer getScore() { return score; }

    public void setScore(Integer score) { this.score = score; }

    public Room getRoom() { return room; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
}
