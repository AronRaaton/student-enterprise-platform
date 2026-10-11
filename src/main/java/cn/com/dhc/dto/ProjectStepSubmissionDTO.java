package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStepSubmissionDTO {
    private Long submissionId;
    private Long projectId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private String studentSchool;
    private Integer stepNumber;
    private String content;
    private String uploadedFile;
    private String status;
    private LocalDateTime submissionTime;
    private String formattedSubmissionTime;
    private LocalDateTime reviewTime;
    private String formattedReviewTime;
    private String feedback;
    private Integer batchNumber;
    private String briefDescription;
    private Boolean isFinalSubmission;
}
