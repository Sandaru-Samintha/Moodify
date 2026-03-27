package com.moodify.service;

import com.moodify.entity.Mood;
import com.moodify.entity.User;
import com.moodify.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyApiService {

    private final RestTemplate restTemplate;
    private final SpotifyAuthService spotifyAuthService;

    /**
     * Create a new playlist in Spotify
     */
    public String createPlaylist(User user, String name, String description) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Get user's Spotify ID
        String userId = getCurrentUserId(user);

        Map<String, Object> playlistData = new HashMap<>();
        playlistData.put("name", name);
        playlistData.put("description", description);
        playlistData.put("public", true);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(playlistData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.spotify.com/v1/users/" + userId + "/playlists",
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("id");
        } else {
            throw new RuntimeException("Failed to create Spotify playlist");
        }
    }

    /**
     * Get track recommendations based on mood
     */
    public List<String> getRecommendations(User user, Mood mood) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());

        StringBuilder urlBuilder = new StringBuilder("https://api.spotify.com/v1/recommendations?limit=20");

        // Add mood-based parameters
        if (mood.getTargetEnergy() != null) {
            urlBuilder.append("&target_energy=").append(mood.getTargetEnergy());
        }
        if (mood.getTargetDanceability() != null) {
            urlBuilder.append("&target_danceability=").append(mood.getTargetDanceability());
        }
        if (mood.getTargetValence() != null) {
            urlBuilder.append("&target_valence=").append(mood.getTargetValence());
        }
        if (mood.getTargetTempo() != null) {
            urlBuilder.append("&target_tempo=").append(mood.getTargetTempo());
        }

        // Add seed genres based on mood
        String seedGenres = getSeedGenresForMood(mood);
        urlBuilder.append("&seed_genres=").append(seedGenres);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> tracks = (List<Map<String, Object>>) responseBody.get("tracks");

            return tracks.stream()
                    .map(track -> (String) track.get("uri"))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Failed to get recommendations from Spotify");
        }
    }

    /**
     * Add tracks to a Spotify playlist
     */
    public void addTracksToPlaylist(User user, String playlistId, List<String> trackUris) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("uris", trackUris);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks",
                request,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to add tracks to playlist");
        }
    }

    /**
     * Get current user's Spotify ID
     */
    private String getCurrentUserId(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("id");
        } else {
            throw new RuntimeException("Failed to get user info from Spotify");
        }
    }

    /**
     * Get seed genres based on mood characteristics
     */
    private String getSeedGenresForMood(Mood mood) {
        List<String> genres = new ArrayList<>();

        // Select genres based on energy level
        if (mood.getTargetEnergy() != null) {
            if (mood.getTargetEnergy() > 0.7) {
                genres.addAll(Arrays.asList("rock", "metal", "electronic", "dance"));
            } else if (mood.getTargetEnergy() < 0.3) {
                genres.addAll(Arrays.asList("acoustic", "classical", "ambient", "chill"));
            } else {
                genres.addAll(Arrays.asList("pop", "indie", "alternative", "r-n-b"));
            }
        }

        // Select genres based on valence (happiness)
        if (mood.getTargetValence() != null) {
            if (mood.getTargetValence() > 0.6) {
                genres.addAll(Arrays.asList("pop", "disco", "funk", "happy"));
            } else if (mood.getTargetValence() < 0.4) {
                genres.addAll(Arrays.asList("blues", "sad", "gothic", "dark"));
            }
        }

        // Select genres based on danceability
        if (mood.getTargetDanceability() != null) {
            if (mood.getTargetDanceability() > 0.6) {
                genres.addAll(Arrays.asList("dance", "disco", "funk", "house"));
            }
        }

        // Ensure we have at least one genre
        if (genres.isEmpty()) {
            genres.addAll(Arrays.asList("pop", "rock", "electronic"));
        }

        // Remove duplicates and limit to 5 genres (Spotify limit)
        return genres.stream()
                .distinct()
                .limit(5)
                .collect(Collectors.joining(","));
    }

    /**
     * Get track recommendations with custom parameters
     */
    public List<String> getCustomRecommendations(User user, Map<String, Object> parameters) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());

        StringBuilder urlBuilder = new StringBuilder("https://api.spotify.com/v1/recommendations?limit=20");

        // Add parameters dynamically
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                urlBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> tracks = (List<Map<String, Object>>) responseBody.get("tracks");

            return tracks.stream()
                    .map(track -> (String) track.get("uri"))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Failed to get custom recommendations from Spotify");
        }
    }

    /**
     * Get track details from Spotify
     */
    public Map<String, Object> getTrackDetails(User user, String trackId) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/tracks/" + trackId,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get track details from Spotify");
        }
    }

    /**
     * Search tracks on Spotify
     */
    public List<Map<String, Object>> searchTracks(User user, String query, int limit) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=" + limit,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> tracks = (Map<String, Object>) responseBody.get("tracks");
            return (List<Map<String, Object>>) tracks.get("items");
        } else {
            throw new RuntimeException("Failed to search tracks on Spotify");
        }
    }

    /**
     * Get playlist details from Spotify
     */
    public Map<String, Object> getPlaylistDetails(User user, String playlistId) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/playlists/" + playlistId,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get playlist details from Spotify");
        }
    }

    /**
     * Remove tracks from playlist
     */
    public void removeTracksFromPlaylist(User user, String playlistId, List<String> trackUris) {
        // Ensure token is valid
        if (!spotifyAuthService.isTokenValid(user)) {
            spotifyAuthService.refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("tracks", trackUris.stream()
                .map(uri -> Map.of("uri", uri))
                .collect(Collectors.toList()));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(
                "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks",
                HttpMethod.DELETE,
                request,
                Map.class
        );
    }
}