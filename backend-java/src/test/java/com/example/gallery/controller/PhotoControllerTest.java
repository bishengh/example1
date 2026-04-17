package com.example.gallery.controller;

import com.example.gallery.model.Album;
import com.example.gallery.model.Photo;
import com.example.gallery.repository.AlbumRepository;
import com.example.gallery.repository.PhotoRepository;
import com.example.gallery.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhotoController.class)
class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoRepository photoRepository;

    @MockBean
    private AlbumRepository albumRepository;

    @MockBean
    private FileStorageService storageService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", Map.of("id", 1L, "username", "admin"));
        return session;
    }

    private Photo createPhoto(Long id, String title) {
        Photo photo = new Photo("test.png", title, "desc", "tag1,tag2", null, OffsetDateTime.now());
        photo.setId(id);
        return photo;
    }

    @Test
    void shouldReturn401WhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/photos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListPhotosWhenLoggedIn() throws Exception {
        MockHttpSession session = loggedInSession();
        Photo photo = createPhoto(1L, "测试照片");
        when(photoRepository.findAll()).thenReturn(List.of(photo));
        when(storageService.buildFileUrl("test.png")).thenReturn("/uploads/test.png");

        mockMvc.perform(get("/api/photos").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("测试照片"));
    }

    @Test
    void shouldGetPhotoDetail() throws Exception {
        MockHttpSession session = loggedInSession();
        Photo photo = createPhoto(1L, "详情照片");
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(storageService.buildFileUrl("test.png")).thenReturn("/uploads/test.png");

        mockMvc.perform(get("/api/photos/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("详情照片"));
    }

    @Test
    void shouldReturn404ForNonexistentPhoto() throws Exception {
        MockHttpSession session = loggedInSession();
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/photos/999").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUploadPhoto() throws Exception {
        MockHttpSession session = loggedInSession();
        MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47});

        Photo saved = createPhoto(1L, "上传照片");
        when(storageService.storeFile(any())).thenReturn("photo.png");
        when(photoRepository.save(any(Photo.class))).thenReturn(saved);
        when(storageService.buildFileUrl("photo.png")).thenReturn("/uploads/photo.png");

        mockMvc.perform(multipart("/api/photos")
                        .file(image)
                        .param("title", "上传照片")
                        .session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("上传照片"));
    }

    @Test
    void shouldRejectUploadWithoutImage() throws Exception {
        MockHttpSession session = loggedInSession();

        mockMvc.perform(multipart("/api/photos")
                        .param("title", "无文件")
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidFileType() throws Exception {
        MockHttpSession session = loggedInSession();
        MockMultipartFile textFile = new MockMultipartFile("image", "doc.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/photos")
                        .file(textFile)
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePhoto() throws Exception {
        MockHttpSession session = loggedInSession();
        Photo photo = createPhoto(1L, "旧标题");
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(photoRepository.save(any(Photo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storageService.buildFileUrl("test.png")).thenReturn("/uploads/test.png");

        mockMvc.perform(put("/api/photos/1")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "新标题", "description", "新描述", "tags", "a,b"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("新标题"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonexistentPhoto() throws Exception {
        MockHttpSession session = loggedInSession();
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/photos/999")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "新标题"))))
                .andExpect(status().isNotFound());
    }
}
