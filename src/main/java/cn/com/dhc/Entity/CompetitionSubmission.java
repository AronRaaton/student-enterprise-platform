package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "competition_submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registration_id")
    @JsonBackReference
    private CompetitionRegistration registration;

    @Column(name = "submission_time", nullable = false)
    private LocalDateTime submissionTime = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "uploaded_file")
    private String uploadedFile;

    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    private String status = "SUBMITTED";

    // 枚举类型定义提交状态
    public enum SubmissionStatus {
        SUBMITTED("已提交"),
        GRADED("已评分"),
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
