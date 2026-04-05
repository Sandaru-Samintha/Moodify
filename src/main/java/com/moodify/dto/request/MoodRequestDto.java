package com.moodify.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodRequestDto {

    @NotBlank(message = "Mood name is required")
    private String name;

    private String description;
    private String color;
    private String icon;

    // Field names match the JSON properties directly
    private Float energy;
    private Float danceability;
    private Float valence;
    private Integer tempo;
}