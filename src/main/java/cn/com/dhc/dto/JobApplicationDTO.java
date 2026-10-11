package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDTO {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long studentId;
    private String studentName;
    private String studentSchool;
    private String studentMajor;
    private LocalDateTime applicationTime;
    private String formattedApplicationTime;
    private String status;
    private String feedback;
    private LocalDateTime reviewTime;
    private String formattedReviewTime;
    
    // 学生技能数据
    private List<SkillDTO> skills;
}
