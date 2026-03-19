package com.moodify.repository;

import com.moodify.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MoodRepository extends JpaRepository<Mood,Long> {

    Optional<Mood> findByName(String name);

    Boolean existsByName(String name);

}
