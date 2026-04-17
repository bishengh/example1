package com.example.gallery.controller;

import com.example.gallery.model.User;
import com.example.gallery.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        User user = new User("admin", "password");
        user.setId(1L);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("admin"));
    }

    @Test
    void shouldRejectLoginWithEmptyCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "", "password", "password"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        User user = new User("admin", "password");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginForNonexistentUser() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "nobody", "password", "password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUserWhenLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", Map.of("id", 1L, "username", "admin"));

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("admin"));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", Map.of("id", 1L, "username", "admin"));

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());
    }
}
