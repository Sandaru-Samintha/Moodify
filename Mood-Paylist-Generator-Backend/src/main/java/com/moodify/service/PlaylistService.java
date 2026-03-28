package com.moodify.service;


import com.moodify.dto.response.PlaylistResponseDto;
import com.moodify.entity.Mood;
import com.moodify.entity.Playlist;
import com.moodify.entity.User;
import com.moodify.exception.ResourceNotFoundException;
import com.moodify.exception.UnauthorizedException;
import com.moodify.repository.MoodRepository;
import com.moodify.repository.PlaylistRepository;
import com.moodify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final MoodRepository moodRepository;
    private final SpotifyApiService spotifyApiService;
    private final ModelMapper modelMapper;

    //Generate a new playlist based on mood
    public PlaylistResponseDto generatePlaylist(String username,Long moodId){
        //Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(()->new ResourceNotFoundException("User not found with username: "));

        //Get mood
        Mood mood = moodRepository.findById(moodId)
                .orElseThrow(()-> new ResourceNotFoundException(("Mood not found with id: " + moodId)));

        //check if user has spotify token
        if(user.getSpotifyAccessToken()==null){
            throw  new UnauthorizedException("User not connected to spotify");
        }

        //generate play list name
        String playlistName = mood.getName() + "Mood - " + LocalDate.now();

        //create the playlist in spotify
        String spotifyPlaylistId = spotifyApiService.createPlaylist(user,playlistName, "Generated based on "+ mood.getName()+" mood");

        //Get playlist in spotify
        List<String> trackUris = spotifyApiService.getRecommendations(user,mood);

        //Add tracks to playlist
        spotifyApiService.addTracksToPlaylist(user,spotifyPlaylistId,trackUris);

        //save playlist to database
        Playlist playlist = new Playlist();
        playlist.setName(playlistName);
        playlist.setDescription("Generated based on " + mood.getName() + " mood");
        playlist.setSpotifyPlaylistId(spotifyPlaylistId);
        playlist.setPlaylistUrl("https://open.spotify.com/playlist" + spotifyPlaylistId);
        playlist.setTrackCount(String.valueOf(trackUris.size()));
        playlist.setIsPublic(true);

        Playlist savedPlaylist = playlistRepository.save(playlist);
        return convertToDto(savedPlaylist);
    }

    //get all playlist for a specific user
    @Cacheable(value = "userPlaylists",key = "#username")
    public List<PlaylistResponseDto> getUserPlayLists(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with username: " + username));

        return  playlistRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    //get All play list by mood
    @Cacheable(value = "moodPlaylist",key = "#moodid")
    public List<PlaylistResponseDto> getPlaylistByMood(Long moodId){

        if(!moodRepository.existsById(moodId)){
            throw new ResourceNotFoundException("Mood not found with id: " + moodId);
        }
        return playlistRepository.findByMoodId(moodId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    //Get playlist by id
    @Cacheable(value = "playlists" , key = "#id")
    public PlaylistResponseDto getPlaylistById(long id){
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Playlist not found with id:"+ id));
        return convertToDto(playlist);
    }

    //Delete playlist by id
    @CacheEvict(value = {"playlists","userPlaylists","moodPlaylists"},allEntries = true)
    public void deletePlaylist(Long id , String username){
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Playlist not found with id: " + id));

        //check id user owns the playlist
        if(!playlist.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("You don't have permission to delete this playlist ");
        }
        playlistRepository.delete(playlist);
    }
    //Get playlists by user ID and mood Id
    public List<PlaylistResponseDto> getPlaylistsByUserAndMood(String username , Long moodId){
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User nor found with username: " + username));

        return  playlistRepository.findByUserIdAndMoodId(user.getId(), moodId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    //get playlists by user id
    public List<PlaylistResponseDto> getPlaylistByUserId(Long userId){
        return playlistRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    //check if user already has playlist for this mood
    public  boolean hasPlaylistForMood(String username , Long moodId){
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with username: " + username));

        List<Playlist> playlistList = playlistRepository.findByUserIdAndMoodId(user.getId(), moodId);
        return !playlistList.isEmpty();
    }





    // Convert Playlist entity to PlaylistResponseDto
    private PlaylistResponseDto convertToDto(Playlist playlist) {
        if (playlist == null) {
            return null;
        }

        PlaylistResponseDto playlistResponseDto = modelMapper.map(playlist, PlaylistResponseDto.class);

        // Set additional fields
        if (playlist.getUser() != null) {
            playlistResponseDto.setUserName(playlist.getUser().getUsername());
        }

        if (playlist.getMood() != null) {
            playlistResponseDto.setMoodName(playlist.getMood().getName());
        }

        return playlistResponseDto;
    }
}


