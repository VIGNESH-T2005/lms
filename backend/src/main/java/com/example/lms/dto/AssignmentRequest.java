package com.example.lms.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AssignmentRequest(
        @NotNull Long courseId,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @FutureOrPresent LocalDate dueDate,
        @NotNull @Min(1) Integer maxScore
) {
}
