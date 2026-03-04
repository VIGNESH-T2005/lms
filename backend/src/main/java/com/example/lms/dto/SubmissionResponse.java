package com.example.lms.dto;

import java.time.LocalDate;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Long studentId,
        String studentName,
        Integer score,
        String status,
        LocalDate submittedAt
) {
}
