package com.moodify.service;

import com.moodify.dto.request.MoodRequestDto;
import com.moodify.dto.response.MoodResponseDto;
import com.moodify.entity.Mood;
import com.moodify.exception.ResourceNotFoundException;
import com.moodify.repository.MoodRepository;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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

    //Create a new mood
    public MoodResponseDto createMood(MoodRequestDto moodRequestDto){
        if(moodRepository.existsByName(moodRequestDto.getName())){
            throw  new RuntimeException(VarList.RSP_DUPLICATED + ": Mood with name '" + moodRequestDto.getName() + "' already exists");
        }

        Mood mood = new Mood();
        mood.setName(moodRequestDto.getName());
        mood.setDescription(moodRequestDto.getDescription());
        mood.setColor(moodRequestDto.getColor());
        mood.setIcon(moodRequestDto.getIcon());
        mood.setTargetDanceability(moodRequestDto.getDanceability());
        mood.setTargetEnergy(moodRequestDto.getEnergy());
        mood.setTargetValence(moodRequestDto.getValence());
        mood.setTargetTempo(moodRequestDto.getTempo());

        Mood savedMood = moodRepository.save(mood);
        return  modelMapper.map(savedMood,MoodResponseDto.class);
    }


    //Get mood by Id with caching
    @Cacheable(value = "moods",key = "#id")
    public MoodResponseDto getMoodById(Long id){
        Mood mood = moodRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Mood not found with id: " + id));
        return  modelMapper.map(mood,MoodResponseDto.class);
    }

    //Get mood by name with caching
    @Cacheable(value = "moods",key = "#name")
    public MoodResponseDto getMoodByName(String name){
        Mood mood = moodRepository.findByName(name)
                .orElseThrow(()->new ResourceNotFoundException("Mood not found with name: " + name));
        return  modelMapper.map(mood, MoodResponseDto.class);
    }

    //Get all moods with caching
    @Cacheable(value = "moods")
    public List<MoodResponseDto> getAllMoods(){
        return  moodRepository.findAll().stream()
                .map(mood -> modelMapper.map(mood, MoodResponseDto.class))
                .collect(Collectors.toList());
    }

    // update the mood information
    @CacheEvict(value = "moods" ,key = "#id")
    public MoodResponseDto updateMood(Long id , MoodRequestDto moodRequestDto){
        Mood mood = moodRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Mood Not found with id : " + id));

        // Check if name is being changed and if new name already exists
        if(!mood.getName().equals(moodRequestDto.getName()) && moodRepository.existsByName(moodRequestDto.getName())){
            throw new RuntimeException(VarList.RSP_DUPLICATED + ": Mood with name '" + moodRequestDto.getName() + "' already exits");
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
        return  modelMapper.map(updatedMood, MoodResponseDto.class);
    }

    // delete mood by id

    @CacheEvict(value = "moods" , key = "#id")
    public void deleteMood(Long id){
        if(!moodRepository.existsById(id)){
            throw  new ResourceNotFoundException("Mood not found with id :" + id);
        }
        moodRepository.deleteById(id);
    }

    //check if mood exists by name
    public boolean existByName(String name){
        return moodRepository.existsByName(name);
    }

    //Get mood by energy level range
    public  List<MoodResponseDto> getMoodEnergyRange(Double minEnergy,Double maxEnergy){
        List<Mood> moodList = moodRepository.findByTargetEnergyBetween(minEnergy, maxEnergy);
        return  moodList.stream()
                .map(mood -> modelMapper.map(mood, MoodResponseDto.class))
                .collect(Collectors.toList());
    }


    // Get mood by valence range (happiness/sadness)
    public List<MoodResponseDto> getMoodsByValenceRange(Double minValence, Double maxValence) {
        List<Mood> moods = moodRepository.findByTargetValenceBetween(minValence, maxValence);
        return moods.stream()
                .map(mood -> modelMapper.map(mood, MoodResponseDto.class))
                .collect(Collectors.toList());
    }

    //Get mood by danceability range

    public List<MoodResponseDto> getMoodsByDanceabilityRange(Double minDanceability, Double maxDanceability) {
        List<Mood> moods = moodRepository.findByTargetDanceabilityBetween(minDanceability, maxDanceability);
        return moods.stream()
                .map(mood -> modelMapper.map(mood, MoodResponseDto.class))
                .collect(Collectors.toList());
    }

}
