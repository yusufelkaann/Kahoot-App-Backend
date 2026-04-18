package com.kahoot_app.Kahoot_App.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kahoot_app.Kahoot_App.enums.RoomStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false)
    private Integer currentQuestionIndex = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.currentQuestionIndex == null) {
            this.currentQuestionIndex = 0;
        }
        if (this.status == null) {
            this.status = RoomStatus.WAITING;
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setRoom(this);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        player.setRoom(null);
    }
}
