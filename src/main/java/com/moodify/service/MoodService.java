package com.moodify.service;

import com.moodify.dto.request.MoodRequestDto;
import com.moodify.dto.response.MoodResponseDto;
import com.moodify.entity.Mood;
import com.moodify.exception.ResourceNotFoundException;
import com.moodify.repository.MoodRepository;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MoodService {

    private final MoodRepository moodRepository;
    private final ModelMapper modelMapper;

    // Create a new mood
    public MoodResponseDto createMood(MoodRequestDto moodRequestDto) {
        if (moodRepository.existsByName(moodRequestDto.getName())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Mood with name '" + moodRequestDto.getName() + "' already exists");
        }

        // Manual mapping from DTO to Entity
        Mood mood = new Mood();
        mood.setName(moodRequestDto.getName());
        mood.setDescription(moodRequestDto.getDescription());
        mood.setColor(moodRequestDto.getColor());
        mood.setIcon(moodRequestDto.getIcon());

        // Map the audio features
        mood.setTargetEnergy(moodRequestDto.getEnergy());
        mood.setTargetDanceability(moodRequestDto.getDanceability());
        mood.setTargetValence(moodRequestDto.getValence());
        mood.setTargetTempo(moodRequestDto.getTempo());

        Mood savedMood = moodRepository.save(mood);

        // Manual mapping from Entity to Response DTO
        MoodResponseDto responseDto = new MoodResponseDto();
        responseDto.setId(savedMood.getId());
        responseDto.setName(savedMood.getName());
        responseDto.setDescription(savedMood.getDescription());
        responseDto.setColor(savedMood.getColor());
        responseDto.setIcon(savedMood.getIcon());
        responseDto.setEnergy(savedMood.getTargetEnergy());
        responseDto.setDanceability(savedMood.getTargetDanceability());
        responseDto.setValence(savedMood.getTargetValence());
        responseDto.setTempo(savedMood.getTargetTempo());

        return responseDto;
    }

    // Get mood by Id with caching
    @Cacheable(value = "moods", key = "#id")
    public MoodResponseDto getMoodById(Long id) {
        Mood mood = moodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mood not found with id: " + id));

        MoodResponseDto responseDto = new MoodResponseDto();
        responseDto.setId(mood.getId());
        responseDto.setName(mood.getName());
        responseDto.setDescription(mood.getDescription());
        responseDto.setColor(mood.getColor());
        responseDto.setIcon(mood.getIcon());
        responseDto.setEnergy(mood.getTargetEnergy());
        responseDto.setDanceability(mood.getTargetDanceability());
        responseDto.setValence(mood.getTargetValence());
        responseDto.setTempo(mood.getTargetTempo());

        return responseDto;
    }

    // Get mood by name with caching
    @Cacheable(value = "moods", key = "#name")
    public MoodResponseDto getMoodByName(String name) {
        Mood mood = moodRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Mood not found with name: " + name));

        MoodResponseDto responseDto = new MoodResponseDto();
        responseDto.setId(mood.getId());
        responseDto.setName(mood.getName());
        responseDto.setDescription(mood.getDescription());
        responseDto.setColor(mood.getColor());
        responseDto.setIcon(mood.getIcon());
        responseDto.setEnergy(mood.getTargetEnergy());
        responseDto.setDanceability(mood.getTargetDanceability());
        responseDto.setValence(mood.getTargetValence());
        responseDto.setTempo(mood.getTargetTempo());

        return responseDto;
    }

    // Get all moods with caching
    @Cacheable(value = "moods")
    public List<MoodResponseDto> getAllMoods() {
        return moodRepository.findAll().stream()
                .map(mood -> {
                    MoodResponseDto responseDto = new MoodResponseDto();
                    responseDto.setId(mood.getId());
                    responseDto.setName(mood.getName());
                    responseDto.setDescription(mood.getDescription());
                    responseDto.setColor(mood.getColor());
                    responseDto.setIcon(mood.getIcon());
                    responseDto.setEnergy(mood.getTargetEnergy());
                    responseDto.setDanceability(mood.getTargetDanceability());
                    responseDto.setValence(mood.getTargetValence());
                    responseDto.setTempo(mood.getTargetTempo());
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    // Update the mood information
    @CacheEvict(value = "moods", key = "#id")
    public MoodResponseDto updateMood(Long id, MoodRequestDto moodRequestDto) {
        Mood mood = moodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mood Not found with id : " + id));

        if (!mood.getName().equals(moodRequestDto.getName()) && moodRepository.existsByName(moodRequestDto.getName())) {
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Mood with name '" + moodRequestDto.getName() + "' already exists");
        }

        mood.setName(moodRequestDto.getName());
        mood.setDescription(moodRequestDto.getDescription());
        mood.setIcon(moodRequestDto.getIcon());
        mood.setColor(moodRequestDto.getColor());
        mood.setTargetEnergy(moodRequestDto.getEnergy());
        mood.setTargetDanceability(moodRequestDto.getDanceability());
        mood.setTargetValence(moodRequestDto.getValence());
        mood.setTargetTempo(moodRequestDto.getTempo());

        Mood updatedMood = moodRepository.save(mood);

        MoodResponseDto responseDto = new MoodResponseDto();
        responseDto.setId(updatedMood.getId());
        responseDto.setName(updatedMood.getName());
        responseDto.setDescription(updatedMood.getDescription());
        responseDto.setColor(updatedMood.getColor());
        responseDto.setIcon(updatedMood.getIcon());
        responseDto.setEnergy(updatedMood.getTargetEnergy());
        responseDto.setDanceability(updatedMood.getTargetDanceability());
        responseDto.setValence(updatedMood.getTargetValence());
        responseDto.setTempo(updatedMood.getTargetTempo());

        return responseDto;
    }

    // Delete mood by id
    @CacheEvict(value = "moods", key = "#id")
    public void deleteMood(Long id) {
        if (!moodRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mood not found with id :" + id);
        }
        moodRepository.deleteById(id);
    }

    // Check if mood exists by name
    public boolean existByName(String name) {
        return moodRepository.existsByName(name);
    }

    // Get mood by energy level range - FIXED: Use Float
    public List<MoodResponseDto> getMoodByEnergyRange(Float minEnergy, Float maxEnergy) {
        List<Mood> moodList = moodRepository.findByTargetEnergyBetween(minEnergy, maxEnergy);
        return moodList.stream()
                .map(mood -> {
                    MoodResponseDto responseDto = new MoodResponseDto();
                    responseDto.setId(mood.getId());
                    responseDto.setName(mood.getName());
                    responseDto.setDescription(mood.getDescription());
                    responseDto.setColor(mood.getColor());
                    responseDto.setIcon(mood.getIcon());
                    responseDto.setEnergy(mood.getTargetEnergy());
                    responseDto.setDanceability(mood.getTargetDanceability());
                    responseDto.setValence(mood.getTargetValence());
                    responseDto.setTempo(mood.getTargetTempo());
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    // Get mood by valence range - FIXED: Use Float
    public List<MoodResponseDto> getMoodsByValenceRange(Float minValence, Float maxValence) {
        List<Mood> moods = moodRepository.findByTargetValenceBetween(minValence, maxValence);
        return moods.stream()
                .map(mood -> {
                    MoodResponseDto responseDto = new MoodResponseDto();
                    responseDto.setId(mood.getId());
                    responseDto.setName(mood.getName());
                    responseDto.setDescription(mood.getDescription());
                    responseDto.setColor(mood.getColor());
                    responseDto.setIcon(mood.getIcon());
                    responseDto.setEnergy(mood.getTargetEnergy());
                    responseDto.setDanceability(mood.getTargetDanceability());
                    responseDto.setValence(mood.getTargetValence());
                    responseDto.setTempo(mood.getTargetTempo());
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    // Get mood by danceability range - FIXED: Use Float
    public List<MoodResponseDto> getMoodsByDanceabilityRange(Float minDanceability, Float maxDanceability) {
        List<Mood> moods = moodRepository.findByTargetDanceabilityBetween(minDanceability, maxDanceability);
        return moods.stream()
                .map(mood -> {
                    MoodResponseDto responseDto = new MoodResponseDto();
                    responseDto.setId(mood.getId());
                    responseDto.setName(mood.getName());
                    responseDto.setDescription(mood.getDescription());
                    responseDto.setColor(mood.getColor());
                    responseDto.setIcon(mood.getIcon());
                    responseDto.setEnergy(mood.getTargetEnergy());
                    responseDto.setDanceability(mood.getTargetDanceability());
                    responseDto.setValence(mood.getTargetValence());
                    responseDto.setTempo(mood.getTargetTempo());
                    return responseDto;
                })
                .collect(Collectors.toList());
    }
}