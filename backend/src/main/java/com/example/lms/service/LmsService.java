package com.example.lms.service;

import com.example.lms.dto.CourseRequest;
import com.example.lms.dto.EnrollmentResponse;
import com.example.lms.dto.StudentRequest;
import com.example.lms.model.Course;
import com.example.lms.model.Enrollment;
import com.example.lms.model.Student;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class LmsService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public LmsService(StudentRepository studentRepository, CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    public List<EnrollmentResponse> getEnrollments() {
        return enrollmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public Student createStudent(StudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student email already exists");
        }

        Student student = Student.builder()
                .name(request.name())
                .email(request.email())
                .build();
        return studentRepository.save(student);
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .instructor(request.instructor())
                .build();
        return courseRepository.save(course);
    }

    @Transactional
    public EnrollmentResponse enroll(Long studentId, Long courseId) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student is already enrolled in this course");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDate.now())
                .status("ENROLLED")
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus()
        );
    }
}
