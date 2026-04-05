package com.moodify.repository;


import com.moodify.entity.Playlist;
import com.moodify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist,Long> {

    List<Playlist> findByUser(User user);

    List<Playlist> findByUserId(Long UserId);

    @Query("SELECT p FROM Playlist p WHERE p.mood.id = :moodId")
    List<Playlist> findByMoodId(@Param("moodId") Long moodId);

    @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId AND p.mood.id = :moodId")
    List<Playlist> findByUserIdAndMoodId(@Param("userId") Long userId, @Param("moodId") Long moodId);
}
