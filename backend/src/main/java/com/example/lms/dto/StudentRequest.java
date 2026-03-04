package com.example.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentRequest(
        @NotBlank String name,
        @Email @NotBlank String email
) {
}
