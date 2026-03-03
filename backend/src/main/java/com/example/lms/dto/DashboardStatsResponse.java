package com.example.lms.dto;

public record DashboardStatsResponse(
        long totalStudents,
        long totalCourses,
        long totalEnrollments,
        long totalAssignments,
        long totalSubmissions,
        double averageScore
) {
}
