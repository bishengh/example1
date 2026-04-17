package com.example.gallery.controller;

import com.example.gallery.model.Album;
import com.example.gallery.model.Photo;
import com.example.gallery.repository.AlbumRepository;
import com.example.gallery.repository.PhotoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlbumController.class)
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlbumRepository albumRepository;

    @MockBean
    private PhotoRepository photoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", Map.of("id", 1L, "username", "admin"));
        return session;
    }

    @Test
    void shouldReturn401WhenNotLoggedInForList() throws Exception {
        mockMvc.perform(get("/api/albums"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListAlbumsWhenLoggedIn() throws Exception {
        MockHttpSession session = loggedInSession();
        when(albumRepository.findAll()).thenReturn(List.of(new Album("旅行", "旅行照片")));

        mockMvc.perform(get("/api/albums").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("旅行"));
    }

    @Test
    void shouldCreateAlbum() throws Exception {
        MockHttpSession session = loggedInSession();
        Album album = new Album("新相册", "描述");
        album.setId(1L);
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        mockMvc.perform(post("/api/albums")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "新相册", "description", "描述"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("新相册"));
    }

    @Test
    void shouldRejectCreateAlbumWithEmptyName() throws Exception {
        MockHttpSession session = loggedInSession();

        mockMvc.perform(post("/api/albums")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "", "description", "描述"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateAlbum() throws Exception {
        MockHttpSession session = loggedInSession();
        Album album = new Album("旧名称", "旧描述");
        album.setId(1L);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        mockMvc.perform(put("/api/albums/1")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "新名称", "description", "新描述"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenUpdatingNonexistentAlbum() throws Exception {
        MockHttpSession session = loggedInSession();
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/albums/999")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "新名称"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAlbumAndUnlinkPhotos() throws Exception {
        MockHttpSession session = loggedInSession();
        Album album = new Album("待删除", "");
        album.setId(1L);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(photoRepository.findByAlbum(album)).thenReturn(List.of());

        mockMvc.perform(delete("/api/albums/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("相册已删除"));
    }

    @Test
    void shouldReturn404WhenDeletingNonexistentAlbum() throws Exception {
        MockHttpSession session = loggedInSession();
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/albums/999").session(session))
                .andExpect(status().isNotFound());
    }
}
