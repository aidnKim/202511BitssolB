package com.example.sideproject01.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlayProgress {
	
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer progressId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "sound_id")
    private Sound sound;
    
    private Integer lastPosition;  // 초 단위
    private Integer duration;      // 전체 길이
    private LocalDateTime updatedAt;
}