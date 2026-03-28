package com.moodify.service;

import com.moodify.entity.User;
import com.moodify.exception.UnauthorizedException;
import com.moodify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SpotifyAuthService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    /**
     * Get Spotify authorization URL for OAuth flow
     */
    public String getAuthorizationUrl() {
        return "https://accounts.spotify.com/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=playlist-modify-public playlist-modify-private playlist-read-private user-top-read" +
                "&show_dialog=true";
    }

    /**
     * Handle Spotify OAuth callback and exchange code for tokens
     */
    public void handleCallback(String code, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> tokenResponse = response.getBody();

            // Set tokens
            user.setSpotifyAccessToken((String) tokenResponse.get("access_token"));
            user.setSpotifyRefreshToken((String) tokenResponse.get("refresh_token"));

            userRepository.save(user);
        } else {
            throw new UnauthorizedException("Failed to get access token from Spotify");
        }
    }

    /**
     * Refresh Spotify access token using refresh token
     */
    public void refreshAccessToken(User user) {
        if (user.getSpotifyRefreshToken() == null) {
            throw new UnauthorizedException("No refresh token available for user: " + user.getUsername());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", user.getSpotifyRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> tokenResponse = response.getBody();

            // Update access token
            user.setSpotifyAccessToken((String) tokenResponse.get("access_token"));

            // If a new refresh token is returned, update it
            if (tokenResponse.containsKey("refresh_token")) {
                user.setSpotifyRefreshToken((String) tokenResponse.get("refresh_token"));
            }

            userRepository.save(user);
        } else {
            throw new UnauthorizedException("Failed to refresh access token for user: " + user.getUsername());
        }
    }

    /**
     * Check if user's Spotify access token is valid
     * Note: Since we don't have token expiry stored, we assume token is valid if it exists
     */
    public boolean isTokenValid(User user) {
        return user.getSpotifyAccessToken() != null;
    }

    /**
     * Get valid access token (refreshes if needed)
     */
    public String getValidAccessToken(User user) {
        // Since we don't have expiry tracking, we'll just return the token
        if (user.getSpotifyAccessToken() == null) {
            throw new UnauthorizedException("No Spotify access token available for user: " + user.getUsername());
        }
        return user.getSpotifyAccessToken();
    }

    /**
     * Get valid access token by username
     */
    public String getValidAccessTokenByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        if (user.getSpotifyAccessToken() == null) {
            throw new UnauthorizedException("No Spotify access token available for user: " + username);
        }
        return user.getSpotifyAccessToken();
    }

    /**
     * Disconnect Spotify account (clear tokens)
     */
    public void disconnectSpotify(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        user.setSpotifyAccessToken(null);
        user.setSpotifyRefreshToken(null);
        userRepository.save(user);
    }

    /**
     * Check if user is connected to Spotify
     */
    public boolean isSpotifyConnected(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        return user.getSpotifyAccessToken() != null;
    }

    /**
     * Get Spotify connection status
     */
    public String getConnectionStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        return user.getSpotifyAccessToken() != null ? "CONNECTED" : "NOT_CONNECTED";
    }

    /**
     * Get Spotify connection details
     */
    public Map<String, Object> getSpotifyConnectionDetails(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        return Map.of(
                "connected", user.getSpotifyAccessToken() != null,
                "hasRefreshToken", user.getSpotifyRefreshToken() != null,
                "status", getConnectionStatus(username)
        );
    }

    /**
     * Refresh token if needed (simplified version without expiry tracking)
     */
    public boolean refreshTokenIfNeeded(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        if (user.getSpotifyRefreshToken() != null) {
            try {
                refreshAccessToken(user);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Force refresh token
     */
    public void forceRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        if (user.getSpotifyRefreshToken() == null) {
            throw new UnauthorizedException("No refresh token available to refresh");
        }

        refreshAccessToken(user);
    }
}