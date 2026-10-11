package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "technical_challenge_submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalChallengeSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "challenge_id")
    private TechnicalChallenge challenge;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;
    
    @Column(name = "solution_description", columnDefinition = "TEXT")
    private String solutionDescription;
    
    @Column(name = "solution_url")
    private String solutionUrl;
    
    @Column(name = "uploaded_file")
    private String uploadedFile;
    
    @Column(nullable = false)
    private String status = "PENDING";
    
    @Column(name = "submit_time", nullable = false)
    private LocalDateTime submitTime = LocalDateTime.now();
    
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    private Integer rating;
    
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
