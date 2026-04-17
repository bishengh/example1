package com.example.gallery.controller;

import com.example.gallery.model.Album;
import com.example.gallery.model.Photo;
import com.example.gallery.repository.AlbumRepository;
import com.example.gallery.repository.PhotoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;

    public AlbumController(AlbumRepository albumRepository, PhotoRepository photoRepository) {
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
    }

    @GetMapping
    public List<Album> list(HttpSession session) {
        if (session.getAttribute("user") == null) {
            throw new RuntimeException("未登录");
        }
        return albumRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> payload, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        String name = payload.get("name");
        String description = payload.getOrDefault("description", "");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "相册名称不能为空"));
        }
        Album album = albumRepository.save(new Album(name, description));
        return ResponseEntity.status(201).body(album);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> payload, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        return albumRepository.findById(id)
                .<ResponseEntity<?>>map(album -> {
                    album.setName(payload.getOrDefault("name", album.getName()));
                    album.setDescription(payload.getOrDefault("description", album.getDescription()));
                    return ResponseEntity.ok(albumRepository.save(album));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "相册未找到")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        return albumRepository.findById(id)
                .map(album -> {
                    List<Photo> assignedPhotos = photoRepository.findByAlbum(album);
                    assignedPhotos.forEach(photo -> photo.setAlbum(null));
                    photoRepository.saveAll(assignedPhotos);
                    albumRepository.delete(album);
                    return ResponseEntity.ok(Map.of("message", "相册已删除"));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "相册未找到")));
    }
}
