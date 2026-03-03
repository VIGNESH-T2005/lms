package com.example.lms.config;

import com.example.lms.model.*;
import com.example.lms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository
    ) {
        return args -> {
            Student alice = studentRepository.findAll().stream().findFirst()
                    .orElseGet(() -> studentRepository.save(Student.builder().name("Alice Johnson").email("alice@example.com").build()));
            Student bob = studentRepository.findAll().stream().skip(1).findFirst()
                    .orElseGet(() -> studentRepository.save(Student.builder().name("Bob Smith").email("bob@example.com").build()));

            Course spring = courseRepository.findAll().stream().findFirst()
                    .orElseGet(() -> courseRepository.save(Course.builder()
                            .title("Java Spring Boot Fundamentals")
                            .description("Build production-ready REST APIs with Spring Boot")
                            .instructor("Dr. Baker")
                            .build()));

            if (!enrollmentRepository.existsByStudentIdAndCourseId(alice.getId(), spring.getId())) {
                enrollmentRepository.save(Enrollment.builder()
                        .student(alice)
                        .course(spring)
                        .enrolledAt(LocalDate.now())
                        .status("ENROLLED")
                        .build());
            }

            if (!enrollmentRepository.existsByStudentIdAndCourseId(bob.getId(), spring.getId())) {
                enrollmentRepository.save(Enrollment.builder()
                        .student(bob)
                        .course(spring)
                        .enrolledAt(LocalDate.now())
                        .status("ENROLLED")
                        .build());
            }

            Assignment assignment = assignmentRepository.findAll().stream().findFirst()
                    .orElseGet(() -> assignmentRepository.save(Assignment.builder()
                            .course(spring)
                            .title("Build a Student CRUD API")
                            .description("Create secure CRUD endpoints for student management.")
                            .dueDate(LocalDate.now().plusDays(10))
                            .maxScore(100)
                            .build()));

            submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), alice.getId())
                    .orElseGet(() -> submissionRepository.save(Submission.builder()
                            .assignment(assignment)
                            .student(alice)
                            .submittedAt(LocalDate.now())
                            .score(88)
                            .status("PASSED")
                            .build()));
        };
    }
}
