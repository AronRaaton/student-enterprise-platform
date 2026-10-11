package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private ProjectCollaboration project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "application_reason", columnDefinition = "TEXT")
    private String applicationReason;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime = LocalDateTime.now();

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "uploaded_file", columnDefinition = "LONGTEXT")
    private String uploadedFile;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "relevant_experience", columnDefinition = "TEXT")
    private String relevantExperience;

    @Column(name = "contact_info")
    private String contactInfo;

    // 枚举类型定义申请状态
    public enum ApplicationStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝");

        private final String displayName;

        ApplicationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
