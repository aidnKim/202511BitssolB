package com.example.sideproject01.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sideproject01.entity.PlayProgress;
import com.example.sideproject01.entity.Sound;
import com.example.sideproject01.entity.User;

public interface PlayProgressRepository extends JpaRepository<PlayProgress, Integer> {
    Optional<PlayProgress> findByUserAndSound(User user, Sound sound);
    List<PlayProgress> findByUser(User user);
}
