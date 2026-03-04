package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.model.*;
import com.example.lms.repository.*;
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
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public LmsService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<StudentResponse> getStudents(String query) {
        List<Student> students = (query == null || query.isBlank())
                ? studentRepository.findAll()
                : studentRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

        return students.stream().map(this::toStudentResponse).toList();
    }

    public List<CourseResponse> getCourses(String query) {
        List<Course> courses = (query == null || query.isBlank())
                ? courseRepository.findAll()
                : courseRepository.findByTitleContainingIgnoreCaseOrInstructorContainingIgnoreCase(query, query);

        return courses.stream().map(this::toCourseResponse).toList();
    }

    public List<EnrollmentResponse> getEnrollments() {
        return enrollmentRepository.findAll().stream().map(this::toEnrollmentResponse).toList();
    }

    public List<AssignmentResponse> getAssignments(Long courseId) {
        List<Assignment> assignments = courseId == null
                ? assignmentRepository.findAll()
                : assignmentRepository.findByCourseId(courseId);

        return assignments.stream().map(this::toAssignmentResponse).toList();
    }

    public List<SubmissionResponse> getSubmissions(Long studentId, Long assignmentId) {
        List<Submission> submissions;
        if (studentId != null) {
            submissions = submissionRepository.findByStudentId(studentId);
        } else if (assignmentId != null) {
            submissions = submissionRepository.findByAssignmentId(assignmentId);
        } else {
            submissions = submissionRepository.findAll();
        }

        return submissions.stream().map(this::toSubmissionResponse).toList();
    }

    public DashboardStatsResponse getDashboardStats() {
        List<Submission> submissions = submissionRepository.findAll();
        double avgScore = submissions.stream().mapToInt(Submission::getScore).average().orElse(0.0);

        return new DashboardStatsResponse(
                studentRepository.count(),
                courseRepository.count(),
                enrollmentRepository.count(),
                assignmentRepository.count(),
                submissionRepository.count(),
                Math.round(avgScore * 100.0) / 100.0
        );
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student email already exists");
        }

        Student student = Student.builder().name(request.name()).email(request.email()).build();
        return toStudentResponse(studentRepository.save(student));
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .instructor(request.instructor())
                .build();
        return toCourseResponse(courseRepository.save(course));
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

        Enrollment saved = enrollmentRepository.save(Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDate.now())
                .status("ENROLLED")
                .build());

        return toEnrollmentResponse(saved);
    }

    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Assignment saved = assignmentRepository.save(Assignment.builder()
                .course(course)
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .maxScore(request.maxScore())
                .build());

        return toAssignmentResponse(saved);
    }

    @Transactional
    public SubmissionResponse createOrUpdateSubmission(SubmissionRequest request) {
        Assignment assignment = assignmentRepository.findById(request.assignmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), assignment.getCourse().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student must be enrolled in course before submitting assignment");
        }

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(request.assignmentId(), request.studentId())
                .orElse(Submission.builder().assignment(assignment).student(student).build());

        submission.setScore(request.score());
        submission.setSubmittedAt(LocalDate.now());
        submission.setStatus(request.score() >= (assignment.getMaxScore() * 0.6) ? "PASSED" : "REVIEW_REQUIRED");

        return toSubmissionResponse(submissionRepository.save(submission));
    }

    private StudentResponse toStudentResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getEmail());
    }

    private CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getInstructor());
    }

    private EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(), enrollment.getStudent().getId(), enrollment.getStudent().getName(),
                enrollment.getCourse().getId(), enrollment.getCourse().getTitle(), enrollment.getEnrolledAt(), enrollment.getStatus()
        );
    }

    private AssignmentResponse toAssignmentResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(), assignment.getCourse().getId(), assignment.getCourse().getTitle(),
                assignment.getTitle(), assignment.getDescription(), assignment.getDueDate(), assignment.getMaxScore()
        );
    }

    private SubmissionResponse toSubmissionResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(), submission.getAssignment().getId(), submission.getAssignment().getTitle(),
                submission.getStudent().getId(), submission.getStudent().getName(),
                submission.getScore(), submission.getStatus(), submission.getSubmittedAt()
        );
    }
}
