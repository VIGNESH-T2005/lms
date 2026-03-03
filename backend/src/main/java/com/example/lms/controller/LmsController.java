package com.example.lms.controller;

import com.example.lms.dto.CourseRequest;
import com.example.lms.dto.EnrollmentRequest;
import com.example.lms.dto.EnrollmentResponse;
import com.example.lms.dto.StudentRequest;
import com.example.lms.model.Course;
import com.example.lms.model.Student;
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

    @GetMapping("/students")
    public List<Student> getStudents() {
        return lmsService.getStudents();
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public Student createStudent(@Valid @RequestBody StudentRequest request) {
        return lmsService.createStudent(request);
    }

    @GetMapping("/courses")
    public List<Course> getCourses() {
        return lmsService.getCourses();
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public Course createCourse(@Valid @RequestBody CourseRequest request) {
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
}
