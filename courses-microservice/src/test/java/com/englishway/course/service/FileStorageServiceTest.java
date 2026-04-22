package com.englishway.course.service;

import com.englishway.course.exception.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    @DisplayName("storeFile: stores a file and sanitizes the name")
    void storeFile_success() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "lesson notes (final).pdf",
            "application/pdf",
            new byte[] {1, 2, 3}
        );

        String relativePath = fileStorageService.storeFile(file, "materials");

        assertThat(relativePath).startsWith("materials/");
        assertThat(relativePath).contains("lesson_notes__final_.pdf");
        assertThat(Files.exists(tempDir.resolve(relativePath))).isTrue();
    }

    @Test
    @DisplayName("storeFile: rejects empty files")
    void storeFile_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.storeFile(file, "materials"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("File is required");
    }

    @Test
    @DisplayName("loadFile: loads an existing file as a resource")
    void loadFile_success() throws IOException {
        Path storedFile = tempDir.resolve("materials").resolve("sample.txt");
        Files.createDirectories(storedFile.getParent());
        Files.write(storedFile, Arrays.asList("hello"));

        Resource resource = fileStorageService.loadFile("materials/sample.txt");

        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    @DisplayName("loadFile: rejects path traversal attempts")
    void loadFile_pathTraversal_throws() {
        assertThatThrownBy(() -> fileStorageService.loadFile("../outside.txt"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid file path");
    }

    @Test
    @DisplayName("deleteFile: removes an existing file")
    void deleteFile_success() throws IOException {
        Path storedFile = tempDir.resolve("materials").resolve("delete-me.txt");
        Files.createDirectories(storedFile.getParent());
        Files.write(storedFile, new byte[] {9});

        fileStorageService.deleteFile("materials/delete-me.txt");

        assertThat(Files.exists(storedFile)).isFalse();
    }

    @Test
    @DisplayName("fileExists: returns true only for files under the upload directory")
    void fileExists_checksPresence() throws IOException {
        Path storedFile = tempDir.resolve("materials").resolve("exists.txt");
        Files.createDirectories(storedFile.getParent());
        Files.write(storedFile, new byte[] {5});

        assertThat(fileStorageService.fileExists("materials/exists.txt")).isTrue();
        assertThat(fileStorageService.fileExists("../outside.txt")).isFalse();
    }

    @Test
    @DisplayName("getFullPath: resolves the file under the upload directory")
    void getFullPath_resolvesPath() {
        Path fullPath = fileStorageService.getFullPath("materials/sample.txt");

        assertThat(fullPath).isEqualTo(tempDir.resolve("materials/sample.txt").normalize());
    }
}