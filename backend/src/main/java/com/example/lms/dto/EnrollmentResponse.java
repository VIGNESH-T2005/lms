package com.example.lms.dto;

import java.time.LocalDate;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        LocalDate enrolledAt,
        String status
) {
}
