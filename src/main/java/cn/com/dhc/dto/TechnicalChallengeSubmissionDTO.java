package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalChallengeSubmissionDTO {
    private Long submissionId;
    private Long challengeId;
    private String challengeTitle;
    private Long studentId;
    private String studentName;
    private String studentSchool;
    private String solutionDescription;
    private String solutionUrl;
    private String uploadedFile;
    private String status;
    private LocalDateTime submitTime;
    private String formattedSubmitTime;
    private LocalDateTime reviewTime;
    private String formattedReviewTime;
    private String feedback;
    private Integer rating;
}
