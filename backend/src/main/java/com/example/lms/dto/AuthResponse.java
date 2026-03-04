package com.example.lms.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        LocalDateTime expiresAt,
        Long userId,
        String fullName,
        String email,
        String role
) {
}
