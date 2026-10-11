package cn.com.dhc.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "training_report")
public class TrainingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "uploaded_file")
    private String uploadedFile;

    @Column(name = "submission_time", nullable = false)
    private LocalDateTime submissionTime;
}
