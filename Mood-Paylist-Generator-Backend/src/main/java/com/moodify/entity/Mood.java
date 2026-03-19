package com.moodify.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "moods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    private String description;

    private  String color;

    private  String icon;

    // Spotify audio features parameters for this mood
    private Float targetEnergy;
    private Float targetDanceability;
    private Float targetValence;
    private Integer targetTempo;

    @OneToMany(mappedBy = "mood")
    private List <Playlist> playlists = new ArrayList<>();



}
