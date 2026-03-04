package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.service.LmsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LmsController {
    private final LmsService lmsService;

    public LmsController(LmsService lmsService) {
        this.lmsService = lmsService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return lmsService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return lmsService.login(request);
    }

    @GetMapping("/auth/me")
    public AuthResponse me(@RequestHeader("X-Auth-Token") String token) {
        return lmsService.me(token);
    }

    @GetMapping("/dashboard/stats")
    public DashboardStatsResponse getDashboardStats() {
        return lmsService.getDashboardStats();
    }

    @GetMapping("/students")
    public List<StudentResponse> getStudents(@RequestParam(required = false) String q) {
        return lmsService.getStudents(q);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse createStudent(@Valid @RequestBody StudentRequest request) {
        return lmsService.createStudent(request);
    }

    @GetMapping("/courses")
    public List<CourseResponse> getCourses(@RequestParam(required = false) String q) {
        return lmsService.getCourses(q);
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(@Valid @RequestBody CourseRequest request) {
        return lmsService.createCourse(request);
    }

    @GetMapping("/enrollments")
    public List<EnrollmentResponse> getEnrollments() {
        return lmsService.getEnrollments();
    }

    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse createEnrollment(@Valid @RequestBody EnrollmentRequest request) {
        return lmsService.enroll(request.studentId(), request.courseId());
    }

    @GetMapping("/assignments")
    public List<AssignmentResponse> getAssignments(@RequestParam(required = false) Long courseId) {
        return lmsService.getAssignments(courseId);
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse createAssignment(@Valid @RequestBody AssignmentRequest request) {
        return lmsService.createAssignment(request);
    }

    @GetMapping("/submissions")
    public List<SubmissionResponse> getSubmissions(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long assignmentId
    ) {
        return lmsService.getSubmissions(studentId, assignmentId);
    }

    @PostMapping("/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse createSubmission(@Valid @RequestBody SubmissionRequest request) {
        return lmsService.createOrUpdateSubmission(request);
    }

    @GetMapping("/attendance")
    public List<AttendanceResponse> getAttendance(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId
    ) {
        return lmsService.getAttendance(studentId, courseId);
    }

    @PostMapping("/attendance")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse markAttendance(@Valid @RequestBody AttendanceRequest request) {
        return lmsService.markAttendance(request);
    }

    @GetMapping("/certifications")
    public List<CertificationResponse> getCertifications(@RequestParam(required = false) Long studentId) {
        return lmsService.getCertifications(studentId);
    }

    @PostMapping("/certifications")
    @ResponseStatus(HttpStatus.CREATED)
    public CertificationResponse issueCertification(@Valid @RequestBody CertificationRequest request) {
        return lmsService.issueCertification(request);
    }
}
