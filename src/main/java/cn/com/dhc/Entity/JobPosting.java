package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String city;

    @Column(name = "min_salary")
    private Integer minSalary;

    @Column(name = "max_salary")
    private Integer maxSalary;

    @Column(name = "job_type", nullable = false)
    private String jobType;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "education_requirement")
    private String educationRequirement;

    @Column(name = "tech_stack", columnDefinition = "TEXT")
    private String techStack;

    @Column(name = "post_time", nullable = false)
    private LocalDateTime postTime = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "OPEN";

    // 枚举类型定义岗位类型
    public enum JobType {
        INTERNSHIP("实习"),
        FULL_TIME("全职");

        private final String displayName;

        JobType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 枚举类型定义岗位状态
    public enum JobStatus {
        OPEN("开放"),
        CLOSED("关闭");

        private final String displayName;

        JobStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
