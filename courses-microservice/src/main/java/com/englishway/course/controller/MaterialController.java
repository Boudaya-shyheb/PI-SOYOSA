package com.englishway.course.controller;

import com.englishway.course.dto.MaterialCreateRequest;
import com.englishway.course.dto.MaterialResponse;
import com.englishway.course.dto.MaterialUpdateRequest;
import com.englishway.course.service.AccessControlService;
import com.englishway.course.service.MaterialService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class MaterialController {
    private final MaterialService materialService;
    private final AccessControlService accessControlService;

    public MaterialController(MaterialService materialService, AccessControlService accessControlService) {
        this.materialService = materialService;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/materials")
    public MaterialResponse createMaterial(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody MaterialCreateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return materialService.createMaterial(context, request);
    }

    @PutMapping("/materials/{materialId}")
    public MaterialResponse updateMaterial(
        @PathVariable("materialId") UUID materialId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody MaterialUpdateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return materialService.updateMaterial(context, materialId, request);
    }

    @GetMapping("/materials/{materialId}")
    public MaterialResponse getMaterial(@PathVariable("materialId") UUID materialId) {
        return materialService.getMaterial(materialId);
    }

    @GetMapping("/lessons/{lessonId}/materials")
    public List<MaterialResponse> listMaterials(@PathVariable("lessonId") UUID lessonId) {
        return materialService.listMaterials(lessonId);
    }

    @PostMapping("/materials/{materialId}/upload")
    public MaterialResponse uploadMaterialFile(
        @PathVariable("materialId") UUID materialId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @RequestParam("file") MultipartFile file
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return materialService.uploadMaterialFile(context, materialId, file);
    }

    @GetMapping("/materials/{materialId}/file")
    public ResponseEntity<Resource> downloadMaterialFile(@PathVariable("materialId") UUID materialId) {
        return materialService.downloadMaterialFile(materialId);
    }

    @DeleteMapping("/materials/{materialId}")
    public void deleteMaterial(
        @PathVariable("materialId") UUID materialId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        materialService.deleteMaterialWithFile(context, materialId);
    }
}
