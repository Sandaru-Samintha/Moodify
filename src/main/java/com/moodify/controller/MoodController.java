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
    public ResponseEntity<ResponseDto> createMood(@Valid @RequestBody MoodRequestDto moodRequestDto) {
        try {
            MoodResponseDto createMood = moodService.createMood(moodRequestDto);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Mood create successful");
            responseDto.setContent(createMood);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllMoods")
    public ResponseEntity<ResponseDto> getAllMoods() {
        try {
            List<MoodResponseDto> allMoods = moodService.getAllMoods();
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Moods retrieved successful");
            responseDto.setContent(allMoods);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getMoodById(@PathVariable Long id) {
        try {
            MoodResponseDto mood = moodService.getMoodById(id);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Mood retrieved successfully");
            responseDto.setContent(mood);
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
            responseDto.setContent(mood);
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
            responseDto.setContent(updatedMood);
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

    // FIXED: Changed @RequestParam from Double to Float
    @GetMapping("/energy-range")
    public ResponseEntity<ResponseDto> getMoodsByEnergyRange(
            @RequestParam Float minEnergy,
            @RequestParam Float maxEnergy) {
        try {
            if (minEnergy > maxEnergy) {
                responseDto.setCode(VarList.RSP_ERROR);
                responseDto.setMessage("minEnergy must be less than or equal to maxEnergy");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
            }

            List<MoodResponseDto> moods = moodService.getMoodByEnergyRange(minEnergy, maxEnergy);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Moods retrieved by energy range successfully");
            responseDto.setContent(moods);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/valence-range")
    public ResponseEntity<ResponseDto> getMoodsByValenceRange(
            @RequestParam Float minValence,
            @RequestParam Float maxValence) {
        try {
            if (minValence > maxValence) {
                responseDto.setCode(VarList.RSP_ERROR);
                responseDto.setMessage("minValence must be less than or equal to maxValence");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
            }

            List<MoodResponseDto> moods = moodService.getMoodsByValenceRange(minValence, maxValence);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Moods retrieved by valence range successfully");
            responseDto.setContent(moods);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/danceability-range")
    public ResponseEntity<ResponseDto> getMoodsByDanceabilityRange(
            @RequestParam Float minDanceability,
            @RequestParam Float maxDanceability) {
        try {
            if (minDanceability > maxDanceability) {
                responseDto.setCode(VarList.RSP_ERROR);
                responseDto.setMessage("minDanceability must be less than or equal to maxDanceability");
                responseDto.setContent(null);
                return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
            }

            List<MoodResponseDto> moods = moodService.getMoodsByDanceabilityRange(minDanceability, maxDanceability);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Moods retrieved by danceability range successfully");
            responseDto.setContent(moods);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}