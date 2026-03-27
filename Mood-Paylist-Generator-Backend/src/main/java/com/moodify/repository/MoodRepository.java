package com.moodify.repository;

import com.moodify.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoodRepository extends JpaRepository<Mood,Long> {
    /**
     * Find mood by name
     */
    Optional<Mood> findByName(String name);

    /**
     * Check if mood exists by name
     */
    boolean existsByName(String name);

    /**
     * Find moods by target energy range
     */
    List<Mood> findByTargetEnergyBetween(Double minEnergy, Double maxEnergy);

    /**
     * Find moods by target valence range
     */
    List<Mood> findByTargetValenceBetween(Double minValence, Double maxValence);

    /**
     * Find moods by target danceability range
     */
    List<Mood> findByTargetDanceabilityBetween(Double minDanceability, Double maxDanceability);

    /**
     * Find moods by target tempo range
     */
    List<Mood> findByTargetTempoBetween(Double minTempo, Double maxTempo);

    /**
     * Find moods by color
     */
    List<Mood> findByColor(String color);

    /**
     * Find moods by icon
     */
    List<Mood> findByIcon(String icon);

    /**
     * Find moods with high energy (above specified threshold)
     */
    @Query("SELECT m FROM Mood m WHERE m.targetEnergy >= :threshold")
    List<Mood> findHighEnergyMoods(@Param("threshold") Double threshold);

    /**
     * Find moods with low energy (below specified threshold)
     */
    @Query("SELECT m FROM Mood m WHERE m.targetEnergy <= :threshold")
    List<Mood> findLowEnergyMoods(@Param("threshold") Double threshold);

    /**
     * Find moods with high valence (happy moods)
     */
    @Query("SELECT m FROM Mood m WHERE m.targetValence >= :threshold")
    List<Mood> findHappyMoods(@Param("threshold") Double threshold);

    /**
     * Find moods with low valence (sad moods)
     */
    @Query("SELECT m FROM Mood m WHERE m.targetValence <= :threshold")
    List<Mood> findSadMoods(@Param("threshold") Double threshold);

    /**
     * Find moods by energy and valence combination
     */
    @Query("SELECT m FROM Mood m WHERE m.targetEnergy BETWEEN :minEnergy AND :maxEnergy " +
            "AND m.targetValence BETWEEN :minValence AND :maxValence")
    List<Mood> findByEnergyAndValenceRange(
            @Param("minEnergy") Double minEnergy,
            @Param("maxEnergy") Double maxEnergy,
            @Param("minValence") Double minValence,
            @Param("maxValence") Double maxValence);

    /**
     * Count moods by energy level
     */
    @Query("SELECT COUNT(m) FROM Mood m WHERE m.targetEnergy >= :threshold")
    Long countHighEnergyMoods(@Param("threshold") Double threshold);

    /**
     * Find moods with specific characteristics
     */
    @Query("SELECT m FROM Mood m WHERE " +
            "(:name IS NULL OR m.name LIKE %:name%) AND " +
            "(:color IS NULL OR m.color = :color) AND " +
            "(:minEnergy IS NULL OR m.targetEnergy >= :minEnergy) AND " +
            "(:maxEnergy IS NULL OR m.targetEnergy <= :maxEnergy)")
    List<Mood> findMoodsByCriteria(
            @Param("name") String name,
            @Param("color") String color,
            @Param("minEnergy") Double minEnergy,
            @Param("maxEnergy") Double maxEnergy);
}