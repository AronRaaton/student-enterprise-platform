package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_collaboration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCollaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_direction", nullable = false)
    private String projectDirection;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "max_participants")
    private Integer maxParticipants = 10;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "collaboration_type")
    private String collaborationType;

    @Column(name = "location")
    private String location;

    @Column(name = "training_plan", columnDefinition = "TEXT")
    private String trainingPlan;

    @Column(name = "time_cycle")
    private String timeCycle;

    @Column(name = "resources", columnDefinition = "TEXT")
    private String resources;

    @Column(name = "support", columnDefinition = "TEXT")
    private String support;

    @Column(name = "tech_requirement", columnDefinition = "TEXT")
    private String techRequirement;

    @Column(name = "project_plan", columnDefinition = "TEXT")
    private String projectPlan;

    @Column(name = "project_plan_file")
    private String projectPlanFile;

    @Column(name = "current_step")
    private Integer currentStep = 1;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectStepSubmission> stepSubmissions = new ArrayList<>();

    // 枚举类型定义项目状态
    public enum ProjectStatus {
        OPEN("开放中"),
        CLOSED("已关闭"),
        COMPLETED("已完成");

        private final String displayName;

        ProjectStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
