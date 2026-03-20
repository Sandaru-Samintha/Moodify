package com.moodify.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyTrackDto {
    private String id;
    private String name;
    private String artist;
    private String album;
    private String uri;
    private String previewUrl;
    private Integer durationMs;
    private Boolean isExplicit;
    private String albumCoverUrl;
}
