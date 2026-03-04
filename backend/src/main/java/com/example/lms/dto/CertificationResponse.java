package com.example.lms.dto;

import java.time.LocalDate;

public record CertificationResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        Integer finalScore,
        LocalDate issuedAt,
        String certificateCode,
        String remarks
) {
}
