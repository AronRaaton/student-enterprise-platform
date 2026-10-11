package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalChallengeDTO {
    private Long challengeId;
    private Long enterpriseId;
    private String enterpriseName;
    private String title;
    private String description;
    private Integer difficulty;
    private String rewardDescription;
    private String requiredSkills;
    private LocalDate deadline;
    private String status;
    private LocalDateTime createTime;
    private String formattedCreateTime;
    private Integer submissionCount;
    private Integer approvedCount;
}
