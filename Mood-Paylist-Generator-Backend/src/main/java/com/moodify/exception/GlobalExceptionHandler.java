package com.moodify.exception;

import com.moodify.dto.ResponseDto;
import com.moodify.util.VarList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ResponseDto responseDto;

    public GlobalExceptionHandler() {
        this.responseDto = new ResponseDto();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
        responseDto.setCode(VarList.RSP_NO_DATA_FOUND);
        responseDto.setMessage(ex.getMessage());
        responseDto.setContent(null);
        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseDto> handleUnauthorizedException(UnauthorizedException ex) {
        responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
        responseDto.setMessage(ex.getMessage());
        responseDto.setContent(null);
        return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseDto> handleBadCredentialsException(BadCredentialsException ex) {
        responseDto.setCode(VarList.RSP_NOT_AUTHORISED);
        responseDto.setMessage("Invalid username or password");
        responseDto.setContent(null);
        return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        responseDto.setCode(VarList.RSP_ERROR);
        responseDto.setMessage("Validation failed");
        responseDto.setContent(errors.toString());
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleGenericException(Exception ex) {
        responseDto.setCode(VarList.RSP_ERROR);
        responseDto.setMessage("An unexpected error occurred: " + ex.getMessage());
        responseDto.setContent(null);
        return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}