package com.example.lms.repository;

import com.example.lms.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByStudentId(Long studentId);
    Optional<Certification> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
