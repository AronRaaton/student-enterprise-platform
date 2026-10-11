package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectApplicationDTO {
    private Long applicationId;
    private Long projectId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private String studentSchool;
    private String applicationReason;
    private String status;
    private LocalDateTime applyTime;
    private String formattedApplyTime;
    private LocalDateTime reviewTime;
    private String formattedReviewTime;
    private String feedback;
    private String uploadedFile;
    private String notes;
    private String relevantExperience;
    private String contactInfo;
}
