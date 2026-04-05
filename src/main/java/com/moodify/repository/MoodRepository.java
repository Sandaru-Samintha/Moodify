package com.moodify.repository;

import com.moodify.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {

    Optional<Mood> findByName(String name);
    boolean existsByName(String name);

    // FIXED: Changed Double to Float
    List<Mood> findByTargetEnergyBetween(Float minEnergy, Float maxEnergy);
    List<Mood> findByTargetValenceBetween(Float minValence, Float maxValence);
    List<Mood> findByTargetDanceabilityBetween(Float minDanceability, Float maxDanceability);

    List<Mood> findByTargetTempoBetween(Integer minTempo, Integer maxTempo);
    List<Mood> findByColor(String color);
    List<Mood> findByIcon(String icon);

    @Query("SELECT m FROM Mood m WHERE m.targetEnergy >= :threshold")
    List<Mood> findHighEnergyMoods(@Param("threshold") Float threshold);

    @Query("SELECT m FROM Mood m WHERE m.targetEnergy <= :threshold")
    List<Mood> findLowEnergyMoods(@Param("threshold") Float threshold);

    @Query("SELECT m FROM Mood m WHERE m.targetValence >= :threshold")
    List<Mood> findHappyMoods(@Param("threshold") Float threshold);

    @Query("SELECT m FROM Mood m WHERE m.targetValence <= :threshold")
    List<Mood> findSadMoods(@Param("threshold") Float threshold);

    @Query("SELECT m FROM Mood m WHERE m.targetEnergy BETWEEN :minEnergy AND :maxEnergy " +
            "AND m.targetValence BETWEEN :minValence AND :maxValence")
    List<Mood> findByEnergyAndValenceRange(
            @Param("minEnergy") Float minEnergy,
            @Param("maxEnergy") Float maxEnergy,
            @Param("minValence") Float minValence,
            @Param("maxValence") Float maxValence);

    @Query("SELECT COUNT(m) FROM Mood m WHERE m.targetEnergy >= :threshold")
    Long countHighEnergyMoods(@Param("threshold") Float threshold);

    @Query("SELECT m FROM Mood m WHERE " +
            "(:name IS NULL OR m.name LIKE %:name%) AND " +
            "(:color IS NULL OR m.color = :color) AND " +
            "(:minEnergy IS NULL OR m.targetEnergy >= :minEnergy) AND " +
            "(:maxEnergy IS NULL OR m.targetEnergy <= :maxEnergy)")
    List<Mood> findMoodsByCriteria(
            @Param("name") String name,
            @Param("color") String color,
            @Param("minEnergy") Float minEnergy,
            @Param("maxEnergy") Float maxEnergy);
}