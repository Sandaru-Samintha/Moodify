package com.moodify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodResponseDto {
    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Float energy;
    private Float danceability;
    private Float valence;
    private Integer tempo;
}