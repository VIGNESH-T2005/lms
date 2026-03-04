package com.example.lms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmissionRequest(
        @NotNull Long assignmentId,
        @NotNull Long studentId,
        @NotNull @Min(0) @Max(1000) Integer score
) {
}
