package com.example.lms.config;

import com.example.lms.model.*;
import com.example.lms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            UserAccountRepository userAccountRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            AttendanceRepository attendanceRepository,
            CertificationRepository certificationRepository
    ) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return args -> {
            if (!userAccountRepository.existsByEmail("admin@lms.com")) {
                userAccountRepository.save(UserAccount.builder()
                        .fullName("LMS Admin")
                        .email("admin@lms.com")
                        .passwordHash(encoder.encode("Admin@123"))
                        .role("ADMIN")
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            Student alice = studentRepository.findAll().stream().findFirst().orElseGet(() ->
                    studentRepository.save(Student.builder().name("Alice Johnson").email("alice@example.com").build()));
            Student bob = studentRepository.findAll().stream().skip(1).findFirst().orElseGet(() ->
                    studentRepository.save(Student.builder().name("Bob Smith").email("bob@example.com").build()));

            Course spring = courseRepository.findAll().stream().findFirst().orElseGet(() ->
                    courseRepository.save(Course.builder()
                            .title("Java Spring Boot Fundamentals")
                            .description("Build production-ready REST APIs with Spring Boot")
                            .instructor("Dr. Baker")
                            .build()));

            if (!enrollmentRepository.existsByStudentIdAndCourseId(alice.getId(), spring.getId())) {
                enrollmentRepository.save(Enrollment.builder().student(alice).course(spring).enrolledAt(LocalDate.now()).status("ENROLLED").build());
            }
            if (!enrollmentRepository.existsByStudentIdAndCourseId(bob.getId(), spring.getId())) {
                enrollmentRepository.save(Enrollment.builder().student(bob).course(spring).enrolledAt(LocalDate.now()).status("ENROLLED").build());
            }

            Assignment assignment = assignmentRepository.findAll().stream().findFirst().orElseGet(() ->
                    assignmentRepository.save(Assignment.builder()
                            .course(spring)
                            .title("Capstone API Assessment")
                            .description("Build and document complete LMS REST APIs.")
                            .dueDate(LocalDate.now().plusDays(7))
                            .maxScore(100)
                            .build()));

            submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), alice.getId()).orElseGet(() ->
                    submissionRepository.save(Submission.builder()
                            .assignment(assignment)
                            .student(alice)
                            .submittedAt(LocalDate.now())
                            .score(90)
                            .status("PASSED")
                            .build()));

            attendanceRepository.findByStudentIdAndCourseIdAndAttendanceDate(alice.getId(), spring.getId(), LocalDate.now())
                    .orElseGet(() -> attendanceRepository.save(Attendance.builder()
                            .student(alice)
                            .course(spring)
                            .attendanceDate(LocalDate.now())
                            .status("PRESENT")
                            .notes("On time")
                            .build()));

            certificationRepository.findByStudentIdAndCourseId(alice.getId(), spring.getId()).orElseGet(() ->
                    certificationRepository.save(Certification.builder()
                            .student(alice)
                            .course(spring)
                            .issuedAt(LocalDate.now())
                            .finalScore(90)
                            .certificateCode("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                            .remarks("Excellent completion")
                            .build()));
        };
    }
}
