package com.englishway.course.service;

import com.englishway.course.exception.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    /**
     * Store a file and return its relative path.
     */
    public String storeFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }

        // Sanitize filename
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Generate unique filename
        String uniqueFilename = UUID.randomUUID().toString() + "_" + sanitizedFilename;
        
        try {
            Path targetDir = uploadDir.resolve(subFolder);
            Files.createDirectories(targetDir);
            
            Path targetPath = targetDir.resolve(uniqueFilename);
            
            // Validate path is still under upload directory (prevent path traversal)
            if (!targetPath.normalize().startsWith(uploadDir)) {
                throw new BadRequestException("Invalid file path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative path for storage in database
            return subFolder + "/" + uniqueFilename;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store file: " + ex.getMessage());
        }
    }

    /**
     * Load a file as a Resource.
     */
    public Resource loadFile(String filePath) {
        try {
            Path path = uploadDir.resolve(filePath).normalize();
            
            // Validate path is still under upload directory
            if (!path.startsWith(uploadDir)) {
                throw new BadRequestException("Invalid file path");
            }

            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("File not found: " + filePath);
            }
        } catch (IOException ex) {
            throw new BadRequestException("Could not read file: " + filePath);
        }
    }

    /**
     * Delete a file.
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            Path path = uploadDir.resolve(filePath).normalize();
            
            // Validate path is still under upload directory
            if (!path.startsWith(uploadDir)) {
                return;
            }

            Files.deleteIfExists(path);
        } catch (IOException ex) {
            // Log but don't throw - file deletion failure shouldn't break the flow
        }
    }

    /**
     * Get the full path for serving files.
     */
    public Path getFullPath(String filePath) {
        return uploadDir.resolve(filePath).normalize();
    }

    /**
     * Check if a file exists.
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        Path path = uploadDir.resolve(filePath).normalize();
        return Files.exists(path) && path.startsWith(uploadDir);
    }
}
