package com.example.gallery.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file) throws IOException;
    String buildFileUrl(String filename);
}
