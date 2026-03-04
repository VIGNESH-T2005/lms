package com.example.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "certifications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "course_id"}),
        @UniqueConstraint(columnNames = {"certificate_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private LocalDate issuedAt;

    @Column(nullable = false)
    private Integer finalScore;

    @Column(name = "certificate_code", nullable = false)
    private String certificateCode;

    @Column(nullable = false)
    private String remarks;
}
