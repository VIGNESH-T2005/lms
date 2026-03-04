package com.example.lms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CertificationRequest(
        @NotNull Long studentId,
        @NotNull Long courseId,
        @NotNull @Min(0) @Max(1000) Integer finalScore,
        String remarks
) {
}
