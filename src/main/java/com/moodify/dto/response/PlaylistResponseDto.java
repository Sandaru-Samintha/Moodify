package com.moodify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponseDto {
    private Long id;
    private String name;
    private String description;
    private String spotifyPlaylistId;
    private String playlistUrl;
    private Integer trackCount;
    private String userName;
    private String moodName;
    private List<String> trackUris;
    private LocalDateTime createdAt;
}
