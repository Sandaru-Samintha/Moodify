package com.moodify.controller;


import com.moodify.dto.ResponseDto;
import com.moodify.dto.response.PlaylistResponseDto;
import com.moodify.service.PlaylistService;
import com.moodify.util.VarList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final ResponseDto responseDto;

    @PostMapping("/generate/{moodId}")
    public ResponseEntity<ResponseDto> generatePlaylist(@PathVariable Long moodId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            List<PlaylistResponseDto> playlists = playlistService.getUserPlayLists(username);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Playlists retrieved successfully");
            responseDto.setContent(String.valueOf(playlists));

            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        }catch (Exception e){
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/user")
    public ResponseEntity<ResponseDto> getUserPlaylists() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            List<PlaylistResponseDto> playlists = playlistService.getUserPlayLists(username);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Playlists retrieved successfully");
            responseDto.setContent(String.valueOf(playlists));
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/mood/{moodId}")
    public ResponseEntity<ResponseDto> getPlaylistsByMood(@PathVariable Long moodId){
        try{
            List<PlaylistResponseDto> playlists = playlistService.getPlaylistByMood(moodId);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Playlists retrieved successfully");
            responseDto.setContent(String.valueOf(playlists));
            return new ResponseEntity<>(responseDto,HttpStatus.OK);
        }catch (Exception e){
            responseDto.setCode(VarList.RSP_ERROR);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getPlaylistById(@PathVariable Long id) {
        try {
            PlaylistResponseDto playlist = playlistService.getPlaylistById(id);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Playlist retrieved successfully");
            responseDto.setContent(String.valueOf(playlist));
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            responseDto.setCode(VarList.RSP_NO_DATA_FOUND);
            responseDto.setMessage(e.getMessage());
            responseDto.setContent(null);
            return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deletePlaylist(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            playlistService.deletePlaylist(id, username);
            responseDto.setCode(VarList.RSP_SUCCESS);
            responseDto.setMessage("Playlist deleted successfully");
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
