package com.moodify.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Playlists")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String spotifyPlaylistId;

    private  String trackCount;

    private String playlistUrl;


    private Boolean isPublic = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_id")
    private Mood mood;

    //Maps a collection of basic/embedded types (not entities) in JPA
    //Creates a separate table for the collection elements
    //@JoinColumn(name = "playlist_id")	Foreign key to parent entity
    //@Column(name = "track_uri")	Column name for the collection values

    @ElementCollection
    @CollectionTable(name = "playlist_tracks",joinColumns = @JoinColumn(name = "playlist_id"))
    @Column(name = "track_uri")
    private List<String> trackUris = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


}
