package com.example.gallery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path storagePath;

    public LocalFileStorageService(@Value("${app.storage-path}") String storagePath) {
        this.storagePath = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建存储目录: " + this.storagePath, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Path target = this.storagePath.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return filename;
    }

    @Override
    public String buildFileUrl(String filename) {
        return "/uploads/" + filename;
    }
}
