package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_step_submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStepSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private ProjectCollaboration project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "step_number")
    private Integer stepNumber;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "uploaded_file")
    private String uploadedFile;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "submission_time", nullable = false)
    private LocalDateTime submissionTime = LocalDateTime.now();

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "batch_number", nullable = false)
    private Integer batchNumber = 1;

    @Column(name = "brief_description", length = 5)
    private String briefDescription;

    @Column(name = "is_final_submission")
    private Boolean isFinalSubmission = false;

    // 枚举类型定义提交状态
    public enum SubmissionStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝");

        private final String displayName;

        SubmissionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
