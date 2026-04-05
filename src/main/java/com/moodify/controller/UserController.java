package com.moodify.controller;

import com.moodify.dto.ResponseDto;
import com.moodify.dto.request.UserRequestDto;
import com.moodify.dto.response.UserResponseDto;
import com.moodify.service.UserService;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ResponseDto responseDto;


    @PostMapping("/register")
    public ResponseEntity<ResponseDto> registerUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        try {
            // Check if username already exists
            if (userService.existByUsername(userRequestDto.getUsername())) {
                responseDto.setCode(VarList.RSP_DUPLICATED);
                responseDto.setMessage("Username already exists");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
            }

            // Check if email already exists
            if (userService.existByEmail(userRequestDto.getEmail())) {
                responseDto.setCode(VarList.RSP_DUPLICATED);
                responseDto.setMessage("Email already exists");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
            }

            UserResponseDto registeredUser = userService.registerUser(userRequestDto);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User registered successfully");
            responseDto.setContent(String.valueOf(registeredUser));
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Registration failed: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            UserResponseDto user = userService.getUserByUsername(username);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User details retrieved successfully");
            responseDto.setContent(user);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_NO_DATA_FOUND);
            responseDto.setMessage("User not found: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto> getUserById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            UserResponseDto currentUser = userService.getUserByUsername(currentUsername);

            // Check if user is requesting their own data or is admin
            if (!currentUser.getId().equals(id) && !hasRole("ADMIN")) {
                responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
                responseDto.setMessage("You can only view your own profile");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.FORBIDDEN);
            }

            UserResponseDto user = userService.getUserById(id);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User retrieved successfully");
            responseDto.setContent(user);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_NO_DATA_FOUND);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto> getUserByUsername(@PathVariable String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // Check if user is requesting their own data or is admin
            if (!currentUsername.equals(username) && !hasRole("ADMIN")) {
                responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
                responseDto.setMessage("You can only view your own profile");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.FORBIDDEN);
            }

            UserResponseDto user = userService.getUserByUsername(username);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User retrieved successfully");
            responseDto.setContent(user);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_NO_DATA_FOUND);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto> getAllUsers() {
        try {
            List<UserResponseDto> users = userService.getAllUsers();
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Users retrieved successfully");
            responseDto.setContent(users);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Failed to retrieve users: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto> updateCurrentUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserResponseDto currentUser = userService.getUserByUsername(username);

            // Prevent email duplication if email is being changed
            if (!currentUser.getEmail().equals(userRequestDto.getEmail()) &&
                    userService.existByEmail(userRequestDto.getEmail())) {
                responseDto.setCode(VarList.RSP_DUPLICATED);
                responseDto.setMessage("Email already exists");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
            }

            // Prevent username duplication if username is being changed
            if (!currentUser.getUsername().equals(userRequestDto.getUsername()) &&
                    userService.existByUsername(userRequestDto.getUsername())) {
                responseDto.setCode(VarList.RSP_DUPLICATED);
                responseDto.setMessage("Username already exists");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
            }

            UserResponseDto updatedUser = userService.updateUser(currentUser.getId(), userRequestDto);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User updated successfully");
            responseDto.setContent(updatedUser);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Update failed: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable Long id,
                                                  @Valid @RequestBody UserRequestDto userRequestDto) {
        try {
            UserResponseDto currentUser = userService.getUserById(id);

            // Prevent email duplication if email is being changed
            if (!currentUser.getEmail().equals(userRequestDto.getEmail()) &&
                    userService.existByEmail(userRequestDto.getEmail())) {
                responseDto.setCode(VarList.RSP_DUPLICATED);
                responseDto.setMessage("Email already exists");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
            }

            // Prevent username duplication if username is being changed
            if (!currentUser.getUsername().equals(userRequestDto.getUsername()) &&
                    userService.existByUsername(userRequestDto.getUsername())) {
                responseDto.setCode(VarList.RSP_DUPLICATED);
                responseDto.setMessage("Username already exists");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
            }

            UserResponseDto updatedUser = userService.updateUser(id, userRequestDto);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User updated successfully");
            responseDto.setContent(updatedUser);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Update failed: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto> deleteCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserResponseDto currentUser = userService.getUserByUsername(username);

            userService.deleteUser(currentUser.getId());
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User account deleted successfully");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Deletion failed: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("User deleted successfully");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Deletion failed: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<ResponseDto> checkUsernameExists(@PathVariable String username) {
        try {
            boolean exists = userService.existByUsername(username);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Username check completed");
            responseDto.setContent(exists);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Check failed: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<ResponseDto> checkEmailExists(@PathVariable String email) {
        try {
            boolean exists = userService.existByEmail(email);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Email check completed");
            responseDto.setContent(exists);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage("Check failed: " + e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to check if current user has admin role

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
    }
}