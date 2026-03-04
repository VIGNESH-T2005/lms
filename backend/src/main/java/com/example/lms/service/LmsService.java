package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.model.*;
import com.example.lms.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LmsService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceRepository attendanceRepository;
    private final CertificationRepository certificationRepository;
    private final UserAccountRepository userAccountRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public LmsService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            AttendanceRepository attendanceRepository,
            CertificationRepository certificationRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.attendanceRepository = attendanceRepository;
        this.certificationRepository = certificationRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String role = request.role().trim().toUpperCase();
        if (!List.of("ADMIN", "INSTRUCTOR", "STUDENT").contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be ADMIN, INSTRUCTOR, or STUDENT");
        }
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User email already exists");
        }

        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        return issueSession(user);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getActive()) || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return issueSession(user);
    }

    public AuthResponse me(String token) {
        UserAccount user = authenticate(token);
        SessionInfo session = sessions.get(token);
        return new AuthResponse(token, session.expiresAt(), user.getId(), user.getFullName(), user.getEmail(), user.getRole());
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
        List<Assignment> assignments = courseId == null ? assignmentRepository.findAll() : assignmentRepository.findByCourseId(courseId);
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

    public List<AttendanceResponse> getAttendance(Long studentId, Long courseId) {
        List<Attendance> records;
        if (studentId != null) {
            records = attendanceRepository.findByStudentId(studentId);
        } else if (courseId != null) {
            records = attendanceRepository.findByCourseId(courseId);
        } else {
            records = attendanceRepository.findAll();
        }
        return records.stream().map(this::toAttendanceResponse).toList();
    }

    public List<CertificationResponse> getCertifications(Long studentId) {
        List<Certification> certifications = studentId == null
                ? certificationRepository.findAll()
                : certificationRepository.findByStudentId(studentId);
        return certifications.stream().map(this::toCertificationResponse).toList();
    }

    public DashboardStatsResponse getDashboardStats() {
        List<Submission> submissions = submissionRepository.findAll();
        List<Attendance> attendance = attendanceRepository.findAll();

        double avgScore = submissions.stream().mapToInt(Submission::getScore).average().orElse(0.0);
        long presentCount = attendance.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        double attendanceRate = attendance.isEmpty() ? 0.0 : (presentCount * 100.0 / attendance.size());

        return new DashboardStatsResponse(
                userAccountRepository.count(),
                studentRepository.count(),
                courseRepository.count(),
                enrollmentRepository.count(),
                assignmentRepository.count(),
                submissionRepository.count(),
                attendanceRepository.count(),
                certificationRepository.count(),
                Math.round(avgScore * 100.0) / 100.0,
                Math.round(attendanceRate * 100.0) / 100.0
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
        Course course = Course.builder().title(request.title()).description(request.description()).instructor(request.instructor()).build();
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
                .student(student).course(course).enrolledAt(LocalDate.now()).status("ENROLLED").build());
        return toEnrollmentResponse(saved);
    }

    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Assignment assignment = assignmentRepository.save(Assignment.builder()
                .course(course)
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .maxScore(request.maxScore())
                .build());
        return toAssignmentResponse(assignment);
    }

    @Transactional
    public SubmissionResponse createOrUpdateSubmission(SubmissionRequest request) {
        Assignment assignment = assignmentRepository.findById(request.assignmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), assignment.getCourse().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student must be enrolled before submitting assessment");
        }

        if (request.score() > assignment.getMaxScore()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score cannot exceed assignment max score");
        }

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(request.assignmentId(), request.studentId())
                .orElse(Submission.builder().assignment(assignment).student(student).build());

        submission.setScore(request.score());
        submission.setSubmittedAt(LocalDate.now());
        submission.setStatus(request.score() >= (assignment.getMaxScore() * 0.6) ? "PASSED" : "REVIEW_REQUIRED");

        return toSubmissionResponse(submissionRepository.save(submission));
    }

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is not enrolled in this course");
        }

        String normalizedStatus = request.status().trim().toUpperCase();
        if (!List.of("PRESENT", "ABSENT", "LATE").contains(normalizedStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attendance status must be PRESENT, ABSENT, or LATE");
        }

        Attendance attendance = attendanceRepository
                .findByStudentIdAndCourseIdAndAttendanceDate(student.getId(), course.getId(), request.attendanceDate())
                .orElse(Attendance.builder().student(student).course(course).attendanceDate(request.attendanceDate()).build());

        attendance.setStatus(normalizedStatus);
        attendance.setNotes(request.notes());
        return toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public CertificationResponse issueCertification(CertificationRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student must be enrolled to receive certification");
        }

        if (request.finalScore() < 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Final score must be at least 60 for certification");
        }

        certificationRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .ifPresent(c -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Certification already issued for this student and course");
                });

        Certification certification = certificationRepository.save(Certification.builder()
                .student(student)
                .course(course)
                .finalScore(request.finalScore())
                .issuedAt(LocalDate.now())
                .remarks((request.remarks() == null || request.remarks().isBlank()) ? "Completed successfully" : request.remarks())
                .certificateCode("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build());

        return toCertificationResponse(certification);
    }

    private AuthResponse issueSession(UserAccount user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(10);
        sessions.put(token, new SessionInfo(user.getId(), expiresAt));
        return new AuthResponse(token, expiresAt, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    private UserAccount authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing auth token");
        }

        SessionInfo session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(LocalDateTime.now())) {
            sessions.remove(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired or invalid token");
        }

        return userAccountRepository.findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private StudentResponse toStudentResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getEmail());
    }

    private CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getInstructor());
    }

    private EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        return new EnrollmentResponse(enrollment.getId(), enrollment.getStudent().getId(), enrollment.getStudent().getName(),
                enrollment.getCourse().getId(), enrollment.getCourse().getTitle(), enrollment.getEnrolledAt(), enrollment.getStatus());
    }

    private AssignmentResponse toAssignmentResponse(Assignment assignment) {
        return new AssignmentResponse(assignment.getId(), assignment.getCourse().getId(), assignment.getCourse().getTitle(),
                assignment.getTitle(), assignment.getDescription(), assignment.getDueDate(), assignment.getMaxScore());
    }

    private SubmissionResponse toSubmissionResponse(Submission submission) {
        return new SubmissionResponse(submission.getId(), submission.getAssignment().getId(), submission.getAssignment().getTitle(),
                submission.getStudent().getId(), submission.getStudent().getName(), submission.getScore(), submission.getStatus(), submission.getSubmittedAt());
    }

    private AttendanceResponse toAttendanceResponse(Attendance attendance) {
        return new AttendanceResponse(attendance.getId(), attendance.getStudent().getId(), attendance.getStudent().getName(),
                attendance.getCourse().getId(), attendance.getCourse().getTitle(), attendance.getAttendanceDate(), attendance.getStatus(), attendance.getNotes());
    }

    private CertificationResponse toCertificationResponse(Certification certification) {
        return new CertificationResponse(certification.getId(), certification.getStudent().getId(), certification.getStudent().getName(),
                certification.getCourse().getId(), certification.getCourse().getTitle(), certification.getFinalScore(),
                certification.getIssuedAt(), certification.getCertificateCode(), certification.getRemarks());
    }

    private record SessionInfo(Long userId, LocalDateTime expiresAt) {
    }
}
