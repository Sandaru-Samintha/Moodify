package com.moodify.controller;

import com.moodify.dto.ResponseDto;
import com.moodify.dto.request.LoginRequestDto;
import com.moodify.dto.request.UserRequestDto;
import com.moodify.dto.response.UserResponseDto;
import com.moodify.service.UserService;
import com.moodify.util.JwtUtil;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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


    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto){
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsernameOrEmail(),loginRequestDto.getPassword()));

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.getUsernameOrEmail());
            final String jwt = jwtUtil.generateToken(userDetails);

            UserResponseDto user = userService.getUserByUsername(userDetails.getUsername());

            Map<String,Object> response = new HashMap<>();
            response.put("token",jwt);
            response.put("user",user);

            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Login Successful");
            responseDto.setContent(String.valueOf(response));
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        catch (Exception e){
            responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
            responseDto.setMessage("Invalid credentials");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto,HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(@Valid @RequestBody UserRequestDto userRequestDto){
        try {
            UserResponseDto registeredUser = userService.registerUser(userRequestDto);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Registration successful");
            responseDto.setContent(String.valueOf(registeredUser));
            return new ResponseEntity<>(responseDto,HttpStatus.CREATED);
        }
        catch (Exception e){
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
