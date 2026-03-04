package com.example.lms.dto;

import java.time.LocalDate;

public record AttendanceResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        LocalDate attendanceDate,
        String status,
        String notes
) {
}
