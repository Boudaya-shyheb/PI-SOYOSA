package com.englishway.course.service;

import com.englishway.course.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryStorageService {
    private final boolean enabled;
    private final String cloudName;
    private final String uploadPreset;
    private final String apiKey;
    private final String apiSecret;
    private final RestTemplate restTemplate;

    public CloudinaryStorageService(
        @Value("${app.cloudinary.enabled:false}") boolean enabled,
        @Value("${app.cloudinary.cloud-name:}") String cloudName,
        @Value("${app.cloudinary.upload-preset:}") String uploadPreset,
        @Value("${app.cloudinary.api-key:}") String apiKey,
        @Value("${app.cloudinary.api-secret:}") String apiSecret
    ) {
        this.enabled = enabled;
        this.cloudName = cloudName == null ? "" : cloudName.trim();
        this.uploadPreset = uploadPreset == null ? "" : uploadPreset.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.apiSecret = apiSecret == null ? "" : apiSecret.trim();
        this.restTemplate = new RestTemplate();
    }

    public boolean isEnabled() {
        return enabled && !cloudName.isBlank() && (isSignedMode() || !uploadPreset.isBlank());
    }

    private boolean isSignedMode() {
        return !apiKey.isBlank() && !apiSecret.isBlank();
    }

    public String uploadMaterialFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        ByteArrayResource fileResource;
        try {
            fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() == null ? "material" : file.getOriginalFilename();
                }
            };
        } catch (Exception ex) {
            throw new BadRequestException("Could not read file: " + ex.getMessage());
        }

        return upload(fileResource, folder);
    }

    public String uploadCourseImageDataUrl(String dataUrl, String folder) {
        if (dataUrl == null || dataUrl.isBlank()) {
            throw new BadRequestException("Image data is required");
        }
        return upload(dataUrl, folder);
    }

    private String upload(Object filePart, String folder) {
        if (!isEnabled()) {
            throw new BadRequestException("Cloudinary is not configured");
        }

        String endpoint = "https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);
        if (folder != null && !folder.isBlank()) {
            body.add("folder", folder);
        }

        if (isSignedMode()) {
            long timestamp = Instant.now().getEpochSecond();
            body.add("api_key", apiKey);
            body.add("timestamp", String.valueOf(timestamp));
            body.add("signature", createSignature(folder, timestamp));
        } else {
            body.add("upload_preset", uploadPreset);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(body, headers), Map.class);
        Map<?, ?> payload = response.getBody();
        if (payload == null) {
            throw new BadRequestException("Cloud upload failed");
        }

        Object secureUrl = payload.get("secure_url");
        if (secureUrl == null || String.valueOf(secureUrl).isBlank()) {
            throw new BadRequestException("Cloud upload did not return URL");
        }

        return String.valueOf(secureUrl);
    }

    private String createSignature(String folder, long timestamp) {
        StringBuilder toSign = new StringBuilder();
        if (folder != null && !folder.isBlank()) {
            toSign.append("folder=").append(folder).append("&");
        }
        toSign.append("timestamp=").append(timestamp);
        return sha1Hex(toSign + apiSecret);
    }

    private String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new BadRequestException("Failed to sign cloud upload request");
        }
    }
}
