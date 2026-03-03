package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String instructor
) {
}
