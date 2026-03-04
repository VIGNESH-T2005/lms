package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AttendanceRequest(
        @NotNull Long studentId,
        @NotNull Long courseId,
        @NotNull LocalDate attendanceDate,
        @NotBlank String status,
        String notes
) {
}
