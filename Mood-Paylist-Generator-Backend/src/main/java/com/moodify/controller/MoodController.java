package com.moodify.controller;


import com.moodify.dto.ResponseDto;
import com.moodify.dto.request.MoodRequestDto;
import com.moodify.dto.response.MoodResponseDto;
import com.moodify.service.MoodService;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/moods")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;
    private final ResponseDto responseDto;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto> createMood(@Valid @RequestBody MoodRequestDto moodRequestDto){
        try {
            MoodResponseDto createMood = moodService.createMood(moodRequestDto);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Mood create successful");
            responseDto.setContent(String.valueOf(createMood));
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        }
        catch (Exception e){
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllMoods")
    public ResponseEntity<ResponseDto> getAllMoods(){
        try {
            List<MoodResponseDto> allMoods = moodService.getAllMoods();
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Moods retrieved successful");
            responseDto.setContent(String.valueOf(allMoods));
            return new ResponseEntity<>(responseDto,HttpStatus.OK);
        }catch (Exception e){
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getMoodById(@PathVariable Long id) {
        try {
            MoodResponseDto mood = moodService.getMoodById(id);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Mood retrieved successfully");
            responseDto.setContent(String.valueOf(mood));
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_NO_DATA_FOUND);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDto> getMoodByName(@PathVariable String name) {
        try {
            MoodResponseDto mood = moodService.getMoodByName(name);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Mood retrieved successfully");
            responseDto.setContent(String.valueOf(mood));
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_NO_DATA_FOUND);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto> updateMood(@PathVariable Long id, @Valid @RequestBody MoodRequestDto moodRequestDto) {
        try {
            MoodResponseDto updatedMood = moodService.updateMood(id, moodRequestDto);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Mood updated successfully");
            responseDto.setContent(String.valueOf(updatedMood));
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto> deleteMood(@PathVariable Long id) {
        try {
            moodService.deleteMood(id);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Mood deleted successfully");
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
