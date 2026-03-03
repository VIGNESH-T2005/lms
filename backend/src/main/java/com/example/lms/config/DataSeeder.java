package com.example.lms.config;

import com.example.lms.model.Course;
import com.example.lms.model.Student;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(StudentRepository studentRepository, CourseRepository courseRepository) {
        return args -> {
            if (studentRepository.count() == 0) {
                studentRepository.save(Student.builder().name("Alice Johnson").email("alice@example.com").build());
                studentRepository.save(Student.builder().name("Bob Smith").email("bob@example.com").build());
            }

            if (courseRepository.count() == 0) {
                courseRepository.save(Course.builder()
                        .title("Java Spring Boot Fundamentals")
                        .description("Build production-ready REST APIs with Spring Boot.")
                        .instructor("Dr. Baker")
                        .build());
                courseRepository.save(Course.builder()
                        .title("React for Modern Web Apps")
                        .description("Develop interactive UIs with hooks and state management.")
                        .instructor("Prof. Rivera")
                        .build());
            }
        };
    }
}
