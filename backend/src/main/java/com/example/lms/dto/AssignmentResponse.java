package com.example.lms.dto;

import java.time.LocalDate;

public record AssignmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String description,
        LocalDate dueDate,
        Integer maxScore
) {
}
