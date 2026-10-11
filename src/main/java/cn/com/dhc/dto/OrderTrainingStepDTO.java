package cn.com.dhc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTrainingStepDTO {
    private Long id;
    private Long projectId;
    private Long userId;
    private String userRole;
    private Integer stepNumber;
    private String title;
    private String content;
    private String fileUrl;
    private LocalDateTime submissionTime;
    
    // Additional fields for display
    private String userName;
    private String userSchool;
}
