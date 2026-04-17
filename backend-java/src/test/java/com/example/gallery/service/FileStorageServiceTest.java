package com.example.gallery.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private Path tempDir;
    private LocalFileStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("uploads-test-");
        service = new LocalFileStorageService(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(tempDir);
    }

    @Test
    void shouldStoreFileAndBuildUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", "content".getBytes());
        String filename = service.storeFile(file);

        assertEquals("test.png", filename);
        assertTrue(Files.exists(tempDir.resolve("test.png")));
        assertEquals("/uploads/test.png", service.buildFileUrl("test.png"));
    }

    @Test
    void shouldCreateStorageDirectoryIfNotExists() throws IOException {
        Path nested = tempDir.resolve("nested").resolve("dir");
        assertFalse(Files.exists(nested));

        LocalFileStorageService newService = new LocalFileStorageService(nested.toString());
        assertTrue(Files.exists(nested));

        FileSystemUtils.deleteRecursively(nested.getParent());
    }
}
