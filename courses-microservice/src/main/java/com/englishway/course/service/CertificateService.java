package com.englishway.course.service;

import com.englishway.course.client.UserServiceClient;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Enrollment;
import com.englishway.course.enums.EnrollmentStatus;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.EnrollmentRepository;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.englishway.course.util.RequestContext;

@Service
public class CertificateService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserServiceFeignService userServiceFeignService;

    public CertificateService(
        CourseRepository courseRepository, 
        EnrollmentRepository enrollmentRepository,
        UserServiceFeignService userServiceFeignService
    ) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userServiceFeignService = userServiceFeignService;
    }

    public ResponseEntity<byte[]> generateCertificate(UUID courseId, String userId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, userId)
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        
        if (enrollment.getStatus() != EnrollmentStatus.COMPLETED && enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BadRequestException("Enrollment is not active or completed");
        }

        String studentName = userId;
        try {
            UserServiceClient.UserInfoDto userInfo = userServiceFeignService.getUserInfo(userId);
            studentName = userInfo.getDisplayName();
        } catch (Exception e) {
            // Fallback to userId
        }
        
        if (studentName != null && studentName.contains("@")) {
            studentName = studentName.split("@")[0];
        }

        String tutorName = course.getTutorId();
        try {
            UserServiceClient.UserInfoDto tutorInfo = userServiceFeignService.getUserInfo(course.getTutorId());
            tutorName = tutorInfo.getDisplayName();
        } catch (Exception e) {
            // Fallback to tutorId
        }

        byte[] pdfBytes = buildPdf(course.getTitle(), studentName, tutorName, enrollment.getId().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("certificate-" + courseId + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private byte[] buildPdf(String courseTitle, String studentName, String tutorName, String certId) {
        try (PDDocument doc = new PDDocument()) {
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscape);
            doc.addPage(page);

            String completedAt = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                // Background Colors / Gradient feel
                content.setNonStrokingColor(new Color(15, 23, 42)); // Dark Slate
                content.addRect(0, 0, landscape.getWidth(), landscape.getHeight());
                content.fill();

                // Subtle Border
                content.setStrokingColor(new Color(56, 189, 248)); // Sky Blue
                content.setLineWidth(15);
                content.addRect(20, 20, landscape.getWidth() - 40, landscape.getHeight() - 40);
                content.stroke();

                // Branding: JUNGLE IN ENGLISH
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 36);
                content.setNonStrokingColor(new Color(56, 189, 248));
                content.newLineAtOffset(60, landscape.getHeight() - 100);
                content.showText("JUNGLE IN ENGLISH");
                content.endText();

                // Title: Certificate of Achievement
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 48);
                content.setNonStrokingColor(Color.WHITE);
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth("CERTIFICATE") / 1000 * 48;
                content.newLineAtOffset((landscape.getWidth() - titleWidth) / 2, landscape.getHeight() / 2 + 100);
                content.showText("CERTIFICATE");
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 24);
                content.setNonStrokingColor(new Color(203, 213, 225));
                float subTitleWidth = PDType1Font.HELVETICA.getStringWidth("OF ACHIEVEMENT") / 1000 * 24;
                content.newLineAtOffset((landscape.getWidth() - subTitleWidth) / 2, landscape.getHeight() / 2 + 70);
                content.showText("OF ACHIEVEMENT");
                content.endText();

                // Student Name
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 32);
                content.setNonStrokingColor(new Color(251, 191, 36)); // Amber
                float nameWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(studentName) / 1000 * 32;
                content.newLineAtOffset((landscape.getWidth() - nameWidth) / 2, landscape.getHeight() / 2 - 20);
                content.showText(studentName);
                content.endText();

                // Course Info
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 18);
                content.setNonStrokingColor(Color.WHITE);
                String courseText = "has successfully completed the course";
                float courseTextWidth = PDType1Font.HELVETICA.getStringWidth(courseText) / 1000 * 18;
                content.newLineAtOffset((landscape.getWidth() - courseTextWidth) / 2, landscape.getHeight() / 2 - 60);
                content.showText(courseText);
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 22);
                content.setNonStrokingColor(new Color(56, 189, 248));
                float courseTitleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(courseTitle) / 1000 * 22;
                content.newLineAtOffset((landscape.getWidth() - courseTitleWidth) / 2, landscape.getHeight() / 2 - 95);
                content.showText(courseTitle);
                content.endText();

                // Certificate ID (subtle)
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 10);
                content.setNonStrokingColor(new Color(71, 85, 105));
                content.newLineAtOffset(60, 45);
                content.showText("Certificate ID: " + certId);
                content.endText();

                // Date
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 14);
                content.setNonStrokingColor(new Color(148, 163, 184));
                content.newLineAtOffset(60, 80);
                content.showText("Date: " + completedAt);
                content.endText();

                // Tutor Column
                content.setStrokingColor(new Color(148, 163, 184));
                content.setLineWidth(1);
                content.moveTo(landscape.getWidth() - 360, 100);
                content.lineTo(landscape.getWidth() - 210, 100);
                content.stroke();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                content.setNonStrokingColor(Color.WHITE);
                content.newLineAtOffset(landscape.getWidth() - 360, 80);
                content.showText(tutorName);
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.setNonStrokingColor(new Color(148, 163, 184));
                content.newLineAtOffset(landscape.getWidth() - 360, 60);
                content.showText("Course Instructor");
                content.endText();

                // JUNGLE IN ENGLISH AUTHORized Column
                content.setStrokingColor(new Color(148, 163, 184));
                content.setLineWidth(1);
                content.moveTo(landscape.getWidth() - 180, 100);
                content.lineTo(landscape.getWidth() - 30, 100);
                content.stroke();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                content.setNonStrokingColor(Color.WHITE);
                content.newLineAtOffset(landscape.getWidth() - 180, 80);
                content.showText("JUNGLE IN ENGLISH");
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.setNonStrokingColor(new Color(148, 163, 184));
                content.newLineAtOffset(landscape.getWidth() - 180, 60);
                content.showText("Certified Platform Authority");
                content.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BadRequestException("Unable to generate certificate: " + ex.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> issueCertificate(RequestContext context, UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        
        if (enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new BadRequestException("Enrollment must be completed to issue certificate");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("certificateId", enrollment.getId());
        response.put("courseId", courseId);
        response.put("status", "ISSUED");
        response.put("issuedAt", Instant.now());
        
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> updateCertificate(RequestContext context, UUID certificateId, Map<String, String> request) {
        // For now, just verify the enrollment exists
        Enrollment enrollment = enrollmentRepository.findById(certificateId)
            .orElseThrow(() -> new NotFoundException("Certificate (enrollment) not found"));
        
        if (!context.getUserId().equals(enrollment.getUserId())) {
            throw new BadRequestException("You can only update your own certificates");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("certificateId", certificateId);
        response.put("status", "UPDATED");
        response.put("updatedAt", Instant.now());
        
        return ResponseEntity.ok(response);
    }

    @Transactional
    public void revokeCertificate(RequestContext context, UUID certificateId) {
        Enrollment enrollment = enrollmentRepository.findById(certificateId)
            .orElseThrow(() -> new NotFoundException("Certificate (enrollment) not found"));
        
        if (!context.getUserId().equals(enrollment.getUserId())) {
            throw new BadRequestException("You can only revoke your own certificates");
        }
        
        // Revoke by setting status back to ACTIVE or PENDING
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(enrollment);
    }
}
