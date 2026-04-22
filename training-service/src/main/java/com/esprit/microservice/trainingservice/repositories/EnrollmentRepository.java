package com.esprit.microservice.trainingservice.repositories;

import com.esprit.microservice.trainingservice.entities.Enrollment;
import com.esprit.microservice.trainingservice.entities.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.training.id = :trainingId")
    Optional<Enrollment> findByStudentIdAndTrainingId(@Param("studentId") Long studentId, @Param("trainingId") int trainingId);

    List<Enrollment> findByStudentId(Long studentId);
    org.springframework.data.domain.Page<Enrollment> findByStudentId(Long studentId, org.springframework.data.domain.Pageable pageable);

    List<Enrollment> findBySessionId(int sessionId);

    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.studentId = :studentId AND e.training.id = :trainingId")
    boolean isEligibleToReview(@Param("studentId") Long studentId, @Param("trainingId") int trainingId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("UPDATE Enrollment e SET e.present = :present WHERE e.id = :enrollmentId")
    void updatePresenceStatus(@Param("enrollmentId") int enrollmentId, @Param("present") boolean present);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("UPDATE Enrollment e SET e.certificateIssued = true, e.certificateIssuedDate = CURRENT_TIMESTAMP WHERE e.id = :enrollmentId")
    void issueCertificateStatus(@Param("enrollmentId") int enrollmentId);

    List<Enrollment> findByStudentIdAndCertificateIssuedTrue(Long studentId);
}
