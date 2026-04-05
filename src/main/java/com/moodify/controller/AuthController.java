package com.moodify.controller;

import com.moodify.dto.ResponseDto;
import com.moodify.dto.request.LoginRequestDto;
import com.moodify.dto.request.UserRequestDto;
import com.moodify.dto.response.UserResponseDto;
import com.moodify.service.SpotifyAuthService;
import com.moodify.service.UserService;
import com.moodify.util.JwtUtil;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ResponseDto responseDto;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SpotifyAuthService spotifyAuthService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            System.out.println("=== LOGIN ATTEMPT ===");
            System.out.println("Username/Email: " + loginRequestDto.getUsernameOrEmail());

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.getUsernameOrEmail());
            System.out.println("User found: " + userDetails.getUsername());

            boolean passwordMatches = passwordEncoder.matches(loginRequestDto.getPassword(), userDetails.getPassword());
            System.out.println("Password matches: " + passwordMatches);

            if (!passwordMatches) {
                throw new BadCredentialsException("Password does not match");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsernameOrEmail(),
                            loginRequestDto.getPassword()
                    )
            );

            System.out.println("Authentication successful: " + authentication.isAuthenticated());

            final String jwt = jwtUtil.generateToken(userDetails);
            UserResponseDto user = userService.getUserByUsername(userDetails.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", user);
            response.put("tokenType", "Bearer");

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Login Successful");
            responseDto.setContent(response);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (BadCredentialsException e) {
            System.err.println("Bad credentials: " + e.getMessage());
            responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
            responseDto.setMessage("Invalid username or password");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
            responseDto.setMessage("Invalid credentials");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(@Valid @RequestBody UserRequestDto userRequestDto) {
        try {
            System.out.println("=== REGISTRATION ATTEMPT ===");
            System.out.println("Username: " + userRequestDto.getUsername());
            System.out.println("Email: " + userRequestDto.getEmail());

            UserResponseDto registeredUser = userService.registerUser(userRequestDto);

            System.out.println("Registration successful for user: " + registeredUser.getUsername());

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Registration successful");
            responseDto.setContent(registeredUser);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<ResponseDto> registerAdmin(@Valid @RequestBody UserRequestDto userRequestDto) {
        try {
            System.out.println("=== ADMIN REGISTRATION ATTEMPT ===");
            System.out.println("Username: " + userRequestDto.getUsername());
            System.out.println("Email: " + userRequestDto.getEmail());

            UserResponseDto registeredAdmin = userService.registerAdmin(userRequestDto);

            System.out.println("Admin registration successful for user: " + registeredAdmin.getUsername());

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Admin user registered successfully");
            responseDto.setContent(registeredAdmin);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("Admin registration error: " + e.getMessage());
            e.printStackTrace();
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== SPOTIFY ENDPOINTS ====================

    /**
     * Get Spotify authorization URL for OAuth flow
     */
    @GetMapping("/spotify/auth-url")
    public ResponseEntity<ResponseDto> getSpotifyAuthUrl(Authentication authentication) {
        try {
            // Get authenticated user
            String username = null;
            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
            }

            if (username == null) {
                responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
                responseDto.setMessage("Please login first");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
            }

            // Encode username to pass in state parameter
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String state = "user=" + encodedUsername;

            String authUrl = spotifyAuthService.getAuthorizationUrl() + "&state=" + state;

            System.out.println("=== SPOTIFY AUTH INITIATED ===");
            System.out.println("Username: " + username);
            System.out.println("State: " + state);

            Map<String, String> response = new HashMap<>();
            response.put("authUrl", authUrl);

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Spotify auth URL generated");
            responseDto.setContent(response);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Failed to generate auth URL: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle Spotify OAuth callback - Returns HTML page
     */
    @GetMapping("/spotify/callback")
    public ResponseEntity<String> handleSpotifyCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        try {
            System.out.println("=== SPOTIFY CALLBACK RECEIVED ===");
            System.out.println("Code: " + (code != null ? code.substring(0, Math.min(30, code.length())) + "..." : "null"));
            System.out.println("State from Spotify: " + state);

            String username = null;

            // Extract username from state parameter
            if (state != null && state.startsWith("user=")) {
                username = state.substring(5); // Remove "user=" prefix
                username = java.net.URLDecoder.decode(username, StandardCharsets.UTF_8.name());
                System.out.println("Username extracted from state: " + username);
            }

            if (username == null) {
                throw new RuntimeException("Unable to identify user. State parameter is missing or invalid.");
            }

            System.out.println("Processing Spotify callback for user: " + username);

            // Exchange code for tokens
            spotifyAuthService.handleCallback(code, username);

            System.out.println("Spotify connected successfully for user: " + username);

            // Return success HTML page
            return ResponseEntity.ok(getSuccessHtml());

        } catch (Exception e) {
            e.printStackTrace();
            String errorHtml = getErrorHtml(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorHtml);
        }
    }

    /**
     * Get Spotify connection status
     */
    @GetMapping("/spotify/status")
    public ResponseEntity<ResponseDto> getSpotifyStatus(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
                responseDto.setMessage("Please login first");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
            }

            String username = authentication.getName();
            Map<String, Object> status = spotifyAuthService.getSpotifyConnectionDetails(username);

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Spotify status retrieved");
            responseDto.setContent(status);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Failed to check status: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Disconnect Spotify account
     */
    @DeleteMapping("/spotify/disconnect")
    public ResponseEntity<ResponseDto> disconnectSpotify(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
                responseDto.setMessage("Please login first");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
            }

            String username = authentication.getName();
            spotifyAuthService.disconnectSpotify(username);

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Spotify disconnected successfully");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Failed to disconnect: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Refresh Spotify token
     */
    @PostMapping("/spotify/refresh")
    public ResponseEntity<ResponseDto> refreshSpotifyToken(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
                responseDto.setMessage("Please login first");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
            }

            String username = authentication.getName();
            spotifyAuthService.forceRefreshToken(username);

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Spotify token refreshed successfully");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Failed to refresh token: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Success HTML page
    private String getSuccessHtml() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Spotify Connected - Moodify</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "            text-align: center;\n" +
                "            padding: 50px;\n" +
                "            background: linear-gradient(135deg, #1DB954 0%, #191414 100%);\n" +
                "            min-height: 100vh;\n" +
                "            margin: 0;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        .container {\n" +
                "            background: white;\n" +
                "            border-radius: 20px;\n" +
                "            padding: 40px;\n" +
                "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
                "            max-width: 500px;\n" +
                "        }\n" +
                "        .success { color: #1DB954; font-size: 64px; margin-bottom: 20px; }\n" +
                "        h1 { color: #333; margin-bottom: 20px; }\n" +
                "        p { color: #666; margin-bottom: 30px; line-height: 1.6; }\n" +
                "        button {\n" +
                "            padding: 12px 30px;\n" +
                "            font-size: 16px;\n" +
                "            cursor: pointer;\n" +
                "            background: #1DB954;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            border-radius: 25px;\n" +
                "            transition: all 0.3s ease;\n" +
                "        }\n" +
                "        button:hover { background: #1aa34a; transform: scale(1.05); }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='success'>✓</div>\n" +
                "        <h1>Spotify Connected Successfully!</h1>\n" +
                "        <p>Your Spotify account has been connected to Moodify.<br>You can now generate mood-based playlists.</p>\n" +
                "        <p><strong>You can close this window and return to the app.</strong></p>\n" +
                "        <button onclick='window.close()'>Close Window</button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    // Error HTML page
    private String getErrorHtml(String errorMessage) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Error - Moodify</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "            text-align: center;\n" +
                "            padding: 50px;\n" +
                "            background: linear-gradient(135deg, #1DB954 0%, #191414 100%);\n" +
                "            min-height: 100vh;\n" +
                "            margin: 0;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        .container {\n" +
                "            background: white;\n" +
                "            border-radius: 20px;\n" +
                "            padding: 40px;\n" +
                "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
                "            max-width: 500px;\n" +
                "        }\n" +
                "        .error { color: #f44336; font-size: 64px; margin-bottom: 20px; }\n" +
                "        h1 { color: #333; margin-bottom: 20px; }\n" +
                "        p { color: #666; margin-bottom: 30px; line-height: 1.6; }\n" +
                "        button {\n" +
                "            padding: 12px 30px;\n" +
                "            font-size: 16px;\n" +
                "            cursor: pointer;\n" +
                "            background: #f44336;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            border-radius: 25px;\n" +
                "            transition: all 0.3s ease;\n" +
                "        }\n" +
                "        button:hover { background: #d32f2f; transform: scale(1.05); }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='error'>✗</div>\n" +
                "        <h1>Connection Failed</h1>\n" +
                "        <p>" + errorMessage + "</p>\n" +
                "        <p>Please close this window, login again, and reconnect Spotify.</p>\n" +
                "        <button onclick='window.close()'>Close Window</button>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}