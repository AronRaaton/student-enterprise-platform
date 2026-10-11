package cn.com.dhc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainingReportDTO {
    private Long id;
    private Long projectId;
    private Long studentId;
    private String title;
    private String content;
    private String uploadedFile;
    private LocalDateTime submissionTime;
    
    // Additional fields for display
    private String studentName;
    private String studentSchool;
}
