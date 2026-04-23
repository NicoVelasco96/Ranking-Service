package com.tournament.ranking.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_rankings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long playerId;

    @Column(nullable = false)
    @Builder.Default
    private Integer eloRating = 1000;

    @Column(nullable = false)
    @Builder.Default
    private Integer wins = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer losses = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer tournamentsPlayed = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer tournamentsWon = 0;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}