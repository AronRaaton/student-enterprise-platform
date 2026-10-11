package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCollaborationDTO {
    private Long projectId;
    private Long enterpriseId;
    private String enterpriseName;
    private String title;
    private String description;
    private String projectDirection;
    private String requiredSkills;
    private Integer maxParticipants;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createTime;
    private String formattedCreateTime;
    private Integer applicationCount;
    private Integer approvedCount;
    private String collaborationType;
    private String location;
    private String trainingPlan;
    private String timeCycle;
    private String resources;
    private String support;
    private String techRequirement;
    private String projectPlan;
    private String projectPlanFile;
    private Integer currentStep;
}
