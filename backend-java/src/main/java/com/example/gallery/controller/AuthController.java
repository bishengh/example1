package com.example.gallery.controller;

import com.example.gallery.model.User;
import com.example.gallery.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpSession session) {
        String username = payload.get("username");
        String password = payload.get("password");
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> {
                    session.setAttribute("user", Map.of("id", user.getId(), "username", user.getUsername()));
                    return ResponseEntity.ok(Map.of("message", "登录成功", "user", Map.of("id", user.getId(), "username", user.getUsername())));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "已退出登录"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        return ResponseEntity.ok(Map.of("user", user));
    }
}
