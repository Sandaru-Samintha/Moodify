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

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SpotifyAuthService {

    @Value("${spotify.client.id:}")
    private String clientId;

    @Value("${spotify.client.secret:}")
    private String clientSecret;

    @Value("${spotify.redirect.uri:http://127.0.0.1:8080/api/auth/spotify/callback}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @PostConstruct
    public void validateCredentials() {
        System.out.println("=== SPOTIFY CREDENTIALS VALIDATION ===");

        if (clientId == null || clientId.isEmpty()) {
            System.err.println("ERROR: Spotify Client ID is not configured!");
            System.err.println("Please set SPOTIFY_CLIENT_ID in .env file or environment variables");
        } else {
            System.out.println("Spotify Client ID: " + clientId.substring(0, Math.min(10, clientId.length())) + "...");
        }

        if (clientSecret == null || clientSecret.isEmpty()) {
            System.err.println("ERROR: Spotify Client Secret is not configured!");
            System.err.println("Please set SPOTIFY_CLIENT_SECRET in .env file or environment variables");
        } else {
            System.out.println("Spotify Client Secret: " + (clientSecret.length() > 0 ? "SET (length: " + clientSecret.length() + ")" : "MISSING"));
        }

        System.out.println("Spotify Redirect URI: " + redirectUri);

        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            System.err.println("WARNING: Spotify integration will not work until credentials are configured!");
        }
    }

    /**
     * Get Spotify authorization URL for OAuth flow
     */
    public String getAuthorizationUrl() {
        if (clientId == null || clientId.isEmpty()) {
            throw new RuntimeException("Spotify Client ID is not configured. Please check your environment variables.");
        }

        return "https://accounts.spotify.com/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=playlist-modify-public playlist-modify-private playlist-read-private user-top-read user-read-email" +
                "&show_dialog=true";
    }

    /**
     * Handle Spotify OAuth callback and exchange code for tokens
     */
    public void handleCallback(String code, String username) {
        // Validate credentials first
        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            throw new UnauthorizedException("Spotify credentials are not configured. Please check server configuration.");
        }

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

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token",
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenResponse = response.getBody();

                // Set access token
                user.setSpotifyAccessToken((String) tokenResponse.get("access_token"));

                // Set refresh token
                user.setSpotifyRefreshToken((String) tokenResponse.get("refresh_token"));

                // Set token expiry date
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                if (expiresIn != null) {
                    user.setTokenExpiryDate(LocalDateTime.now().plusSeconds(expiresIn));
                    System.out.println("Token expiry set to: " + user.getTokenExpiryDate());
                } else {
                    user.setTokenExpiryDate(LocalDateTime.now().plusHours(1));
                }

                userRepository.save(user);
                System.out.println("Spotify tokens saved for user: " + username);
                System.out.println("Access Token: " + user.getSpotifyAccessToken().substring(0, Math.min(20, user.getSpotifyAccessToken().length())) + "...");
                System.out.println("Expires in: " + expiresIn + " seconds");
                System.out.println("Expires at: " + user.getTokenExpiryDate());
            } else {
                throw new UnauthorizedException("Failed to get access token from Spotify. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to exchange code for tokens: " + e.getMessage());
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

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token",
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> tokenResponse = response.getBody();

                // Update access token
                user.setSpotifyAccessToken((String) tokenResponse.get("access_token"));

                // Update token expiry date
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                if (expiresIn != null) {
                    user.setTokenExpiryDate(LocalDateTime.now().plusSeconds(expiresIn));
                }

                // If a new refresh token is returned, update it
                if (tokenResponse.containsKey("refresh_token")) {
                    user.setSpotifyRefreshToken((String) tokenResponse.get("refresh_token"));
                }

                userRepository.save(user);
                System.out.println("Spotify tokens refreshed for user: " + user.getUsername());
                System.out.println("New expiry date: " + user.getTokenExpiryDate());
            } else {
                throw new UnauthorizedException("Failed to refresh access token. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Check if user's Spotify access token is valid
     */
    public boolean isTokenValid(User user) {
        if (user.getSpotifyAccessToken() == null) {
            return false;
        }

        if (user.getTokenExpiryDate() != null) {
            boolean isValid = user.getTokenExpiryDate().isAfter(LocalDateTime.now());
            System.out.println("Token valid: " + isValid);
            return isValid;
        }

        return true;
    }

    /**
     * Get valid access token (refreshes if needed)
     */
    public String getValidAccessToken(User user) {
        if (user.getSpotifyAccessToken() == null) {
            throw new UnauthorizedException("No Spotify access token available for user: " + user.getUsername());
        }

        if (!isTokenValid(user) && user.getSpotifyRefreshToken() != null) {
            System.out.println("Token expired, refreshing...");
            refreshAccessToken(user);
        }

        return user.getSpotifyAccessToken();
    }

    /**
     * Get valid access token by username
     */
    public String getValidAccessTokenByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        return getValidAccessToken(user);
    }

    /**
     * Disconnect Spotify account (clear tokens)
     */
    public void disconnectSpotify(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        user.setSpotifyAccessToken(null);
        user.setSpotifyRefreshToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        System.out.println("Spotify disconnected for user: " + username);
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

        if (user.getSpotifyAccessToken() == null) {
            return "NOT_CONNECTED";
        }

        if (isTokenValid(user)) {
            return "CONNECTED_VALID";
        } else {
            return "CONNECTED_EXPIRED";
        }
    }

    /**
     * Get Spotify connection details
     */
    public Map<String, Object> getSpotifyConnectionDetails(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        Map<String, Object> details = new HashMap<>();
        details.put("connected", user.getSpotifyAccessToken() != null);
        details.put("hasRefreshToken", user.getSpotifyRefreshToken() != null);
        details.put("status", getConnectionStatus(username));
        details.put("tokenValid", isTokenValid(user));
        details.put("tokenExpiryDate", user.getTokenExpiryDate());

        return details;
    }

    /**
     * Refresh token if needed
     */
    public boolean refreshTokenIfNeeded(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + username));

        if (!isTokenValid(user) && user.getSpotifyRefreshToken() != null) {
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