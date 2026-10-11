package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingDTO {
    private Long jobId;
    private Long enterpriseId;
    private String enterpriseName;
    private String title;
    private String description;
    private String city;
    private Integer minSalary;
    private Integer maxSalary;
    private String jobType;
    private String requirements;
    private LocalDateTime postTime;
    private String status;
    private String formattedPostTime; // 格式化后的发布时间，用于前端显示
    private String positionTitle;
    private String educationRequirement;
    private String techStack;

    // 企业相关信息
    private String enterpriseType;
    private String enterpriseLocation;
    private Integer enterpriseEmployeeCount;
    private String enterpriseWebsite;
    private String enterpriseIndustry;
}
