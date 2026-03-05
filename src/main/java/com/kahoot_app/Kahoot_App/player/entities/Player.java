package com.kahoot_app.Kahoot_App.player.entities;

import java.time.LocalDateTime;

import com.kahoot_app.Kahoot_App.player.enums.PlayerRole;
import com.kahoot_app.Kahoot_App.room.entities.Room;
import com.kahoot_app.Kahoot_App.room.enums.RoomStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
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
    @Column()
    private PlayerRole role;

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @PrePersist
    public void onCreate() {
        if (this.role == null) {
            this.role = PlayerRole.PLAYER;
        }
    }

    public Player() {}

    public Player(String nickname, Room room, PlayerRole role) {
        this.nickname = nickname;
        this.room = room;
        this.score = 0;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getNickname() { return nickname; }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getScore() { return score; }

    public void setScore(int score) { this.score = score; }

    public Room getRoom() { return room; }

    public void setRoom(Room room) {
        this.room = room;
    }

    public PlayerRole getRole() { return role; }

    public void setRole(PlayerRole role) {
        this.role = role;
    }



    public LocalDateTime getJoinedAt() { return joinedAt; }
}
