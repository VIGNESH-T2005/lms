package com.example.lms.repository;

import com.example.lms.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTitleContainingIgnoreCaseOrInstructorContainingIgnoreCase(String title, String instructor);
}
