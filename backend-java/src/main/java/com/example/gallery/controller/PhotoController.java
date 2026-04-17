package com.example.gallery.controller;

import com.example.gallery.model.Album;
import com.example.gallery.model.Photo;
import com.example.gallery.repository.AlbumRepository;
import com.example.gallery.repository.PhotoRepository;
import com.example.gallery.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final FileStorageService storageService;

    public PhotoController(PhotoRepository photoRepository, AlbumRepository albumRepository, FileStorageService storageService) {
        this.photoRepository = photoRepository;
        this.albumRepository = albumRepository;
        this.storageService = storageService;
    }

    @GetMapping
    public List<Map<String, Object>> list(HttpSession session) {
        requireLogin(session);
        return photoRepository.findAll().stream()
                .map(this::mapPhoto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, HttpSession session) {
        requireLogin(session);
        return photoRepository.findById(id)
                .map(photo -> ResponseEntity.ok(mapPhoto(photo)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "照片未找到")));
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile image,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String tags,
                                    @RequestParam(required = false) Long albumId,
                                    HttpSession session) throws IOException {
        requireLogin(session);
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请上传照片"));
        }
        String contentType = image.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "只支持 JPG、PNG、GIF 格式"));
        }

        String filename = storageService.storeFile(image);
        Album album = null;
        if (albumId != null) {
            album = albumRepository.findById(albumId).orElse(null);
        }
        Photo photo = new Photo(filename,
                title == null ? "" : title,
                description == null ? "" : description,
                tags == null ? "" : tags,
                album,
                OffsetDateTime.now());
        Photo saved = photoRepository.save(photo);
        return ResponseEntity.status(201).body(mapPhoto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, Object> payload,
                                    HttpSession session) {
        requireLogin(session);
        return photoRepository.findById(id)
                .map(photo -> {
                    photo.setTitle((String) payload.getOrDefault("title", photo.getTitle()));
                    photo.setDescription((String) payload.getOrDefault("description", photo.getDescription()));
                    photo.setTags((String) payload.getOrDefault("tags", photo.getTags()));
                    Object albumId = payload.get("albumId");
                    if (albumId instanceof Number) {
                        albumRepository.findById(((Number) albumId).longValue()).ifPresent(photo::setAlbum);
                    }
                    return ResponseEntity.ok(mapPhoto(photoRepository.save(photo)));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "照片未找到")));
    }

    private void requireLogin(HttpSession session) {
        if (session.getAttribute("user") == null) {
            throw new RuntimeException("未登录");
        }
    }

    private Map<String, Object> mapPhoto(Photo photo) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", photo.getId());
        map.put("url", storageService.buildFileUrl(photo.getFilename()));
        map.put("title", photo.getTitle());
        map.put("description", photo.getDescription());
        map.put("tags", photo.getTags() == null ? List.of() : List.of(photo.getTags().split(",")).stream().filter(tag -> !tag.isBlank()).collect(Collectors.toList()));
        map.put("uploadedAt", photo.getUploadedAt().toString());
        map.put("albumId", photo.getAlbum() == null ? null : photo.getAlbum().getId());
        return map;
    }
}
