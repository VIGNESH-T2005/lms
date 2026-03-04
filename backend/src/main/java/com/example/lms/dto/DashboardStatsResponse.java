package com.example.lms.dto;

public record DashboardStatsResponse(
        long totalUsers,
        long totalStudents,
        long totalCourses,
        long totalEnrollments,
        long totalAssignments,
        long totalSubmissions,
        long totalAttendanceRecords,
        long totalCertifications,
        double averageScore,
        double attendanceRate
) {
}
