package com.moodify.service;

import com.moodify.dto.request.UserRequestDto;
import com.moodify.dto.response.UserResponseDto;
import com.moodify.entity.Role;
import com.moodify.entity.User;
import com.moodify.exception.ResourceNotFoundException;
import com.moodify.repository.UserRepository;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    // Register new user
    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        if (userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Username already exists");
        }
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Email already exists");
        }

        User user = modelMapper.map(userRequestDto, User.class);
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRole(Role.USER);  // Set default role

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    // Register new admin user
    public UserResponseDto registerAdmin(UserRequestDto userRequestDto) {
        if (userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Username already exists");
        }
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Email already exists");
        }

        User user = modelMapper.map(userRequestDto, User.class);
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRole(Role.ADMIN);  // Set ADMIN role

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        System.out.println("Admin user created: " + savedUser.getUsername());
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    // Get user by Id
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + id));

        UserResponseDto response = modelMapper.map(user, UserResponseDto.class);
        response.setHasSpotifyToken(user.getSpotifyAccessToken() != null);
        return response;
    }

    // Get user by username
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username : " + username));

        UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);
        userResponseDto.setHasSpotifyToken(user.getSpotifyAccessToken() != null);
        return userResponseDto;
    }

    // Update user information
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equals(userRequestDto.getUsername()) &&
                userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Username already exists");
        }

        if (!user.getEmail().equals(userRequestDto.getEmail()) &&
                userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Email already exists");
        }

        user.setUsername(userRequestDto.getUsername());
        user.setEmail(userRequestDto.getEmail());

        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updateUser = userRepository.save(user);

        UserResponseDto userResponseDto = modelMapper.map(updateUser, UserResponseDto.class);
        userResponseDto.setHasSpotifyToken(updateUser.getSpotifyAccessToken() != null);
        return userResponseDto;
    }

    // Delete user by Id
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id : " + id);
        }
        userRepository.deleteById(id);
    }

    // Check if username exists
    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Check if email exists
    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Get all users
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserResponseDto dto = modelMapper.map(user, UserResponseDto.class);
                    dto.setHasSpotifyToken(user.getSpotifyAccessToken() != null);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}