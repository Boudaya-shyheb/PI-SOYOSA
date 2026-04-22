package com.englishway.course.service;

import com.englishway.course.dto.MaterialCreateRequest;
import com.englishway.course.dto.MaterialResponse;
import com.englishway.course.dto.MaterialUpdateRequest;
import com.englishway.course.entity.LearningMaterial;
import com.englishway.course.entity.Lesson;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.LearningMaterialRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.util.RequestContext;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MaterialService {
    private final LearningMaterialRepository materialRepository;
    private final LessonRepository lessonRepository;
    private final AccessControlService accessControlService;
    private final FileStorageService fileStorageService;
    private final CloudinaryStorageService cloudinaryStorageService;

    public MaterialService(
        LearningMaterialRepository materialRepository,
        LessonRepository lessonRepository,
        AccessControlService accessControlService,
        FileStorageService fileStorageService,
        CloudinaryStorageService cloudinaryStorageService
    ) {
        this.materialRepository = materialRepository;
        this.lessonRepository = lessonRepository;
        this.accessControlService = accessControlService;
        this.fileStorageService = fileStorageService;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @Transactional
    public MaterialResponse createMaterial(RequestContext context, MaterialCreateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Lesson lesson = lessonRepository.findById(request.getLessonId())
            .orElseThrow(() -> new NotFoundException("Lesson not found"));
        accessControlService.requireCourseOwnership(context, lesson.getChapter().getCourse().getTutorId());
        LearningMaterial material = new LearningMaterial();
        material.setLesson(lesson);
        material.setType(request.getType());
        material.setTitle(request.getTitle());
        material.setUrl(request.getUrl());
        material.setContent(request.getContent());
        LearningMaterial saved = materialRepository.save(material);
        return toResponse(saved);
    }

    @Transactional
    public MaterialResponse updateMaterial(RequestContext context, UUID materialId, MaterialUpdateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        LearningMaterial material = materialRepository.findById(materialId)
            .orElseThrow(() -> new NotFoundException("Material not found"));
        accessControlService.requireCourseOwnership(context, material.getLesson().getChapter().getCourse().getTutorId());
        material.setType(request.getType());
        material.setTitle(request.getTitle());
        material.setUrl(request.getUrl());
        material.setContent(request.getContent());
        LearningMaterial saved = materialRepository.save(material);
        return toResponse(saved);
    }

    @Transactional
    public void deleteMaterial(RequestContext context, UUID materialId) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        LearningMaterial material = materialRepository.findById(materialId)
            .orElseThrow(() -> new NotFoundException("Material not found"));
        accessControlService.requireCourseOwnership(context, material.getLesson().getChapter().getCourse().getTutorId());
        materialRepository.deleteById(materialId);
    }

    public List<MaterialResponse> listMaterials(UUID lessonId) {
        return materialRepository.findByLessonId(lessonId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public MaterialResponse getMaterial(UUID materialId) {
        LearningMaterial material = materialRepository.findById(materialId)
            .orElseThrow(() -> new NotFoundException("Material not found"));
        return toResponse(material);
    }

    @Transactional
    public MaterialResponse uploadMaterialFile(RequestContext context, UUID materialId, MultipartFile file) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        LearningMaterial material = materialRepository.findById(materialId)
            .orElseThrow(() -> new NotFoundException("Material not found"));
        accessControlService.requireCourseOwnership(context, material.getLesson().getChapter().getCourse().getTutorId());
        
        // Delete old file if exists
        if (material.getFilePath() != null && !isCloudUrl(material.getFilePath())) {
            fileStorageService.deleteFile(material.getFilePath());
        }
        
        String subFolder = "materials/" + material.getLesson().getId();
        String filePath;
        if (cloudinaryStorageService.isEnabled()) {
            filePath = cloudinaryStorageService.uploadMaterialFile(file, subFolder);
        } else {
            // Fallback if cloud env vars are not configured yet.
            filePath = fileStorageService.storeFile(file, subFolder);
        }
        
        material.setFileName(file.getOriginalFilename());
        material.setFileType(file.getContentType());
        material.setFilePath(filePath);
        material.setFileData(null); // Clear blob data if any
        
        LearningMaterial saved = materialRepository.save(material);
        return toResponse(saved);
    }

    public ResponseEntity<Resource> downloadMaterialFile(UUID materialId) {
        LearningMaterial material = materialRepository.findById(materialId)
            .orElseThrow(() -> new NotFoundException("Material not found"));

        if (material.getFilePath() != null && isCloudUrl(material.getFilePath())) {
            return ResponseEntity.status(302).location(URI.create(material.getFilePath())).build();
        }
        
        // First try to load from file path (new storage)
        if (material.getFilePath() != null && fileStorageService.fileExists(material.getFilePath())) {
            Resource resource = fileStorageService.loadFile(material.getFilePath());
            String fileName = material.getFileName() == null ? "material" : material.getFileName();
            String fileType = material.getFileType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : material.getFileType();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileType));
            headers.setContentDisposition(ContentDisposition.inline().filename(fileName).build());
            return ResponseEntity.ok().headers(headers).body(resource);
        }
        
        // Fallback to blob data (legacy storage)
        if (material.getFileData() == null || material.getFileData().length == 0) {
            throw new NotFoundException("File not found");
        }
        String fileName = material.getFileName() == null ? "material" : material.getFileName();
        String fileType = material.getFileType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : material.getFileType();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileType));
        headers.setContentDisposition(ContentDisposition.inline().filename(fileName).build());
        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(material.getFileData());
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @Transactional
    public void deleteMaterialWithFile(RequestContext context, UUID materialId) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        LearningMaterial material = materialRepository.findById(materialId)
            .orElseThrow(() -> new NotFoundException("Material not found"));
        accessControlService.requireCourseOwnership(context, material.getLesson().getChapter().getCourse().getTutorId());
        
        // Delete associated file
        if (material.getFilePath() != null && !isCloudUrl(material.getFilePath())) {
            fileStorageService.deleteFile(material.getFilePath());
        }
        
        materialRepository.deleteById(materialId);
    }

    private MaterialResponse toResponse(LearningMaterial material) {
        MaterialResponse response = new MaterialResponse();
        response.setId(material.getId());
        response.setLessonId(material.getLesson().getId());
        response.setType(material.getType());
        response.setTitle(material.getTitle());
        response.setUrl(material.getUrl());
        response.setContent(material.getContent());
        response.setFileName(material.getFileName());
        response.setFileType(material.getFileType());
        boolean hasBlobFile = material.getFileData() != null && material.getFileData().length > 0;
        boolean hasDiskFile = material.getFilePath() != null && !material.getFilePath().isBlank();
        response.setHasFile(hasBlobFile || hasDiskFile);
        response.setCreatedAt(material.getCreatedAt());
        return response;
    }

    private boolean isCloudUrl(String value) {
        return value != null && (value.startsWith("https://") || value.startsWith("http://"));
    }
}
