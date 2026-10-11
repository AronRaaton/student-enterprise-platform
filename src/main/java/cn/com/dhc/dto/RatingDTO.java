package cn.com.dhc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RatingDTO {
    private Long id;
    private Long targetId; // 可以是任务ID或企业ID
    private Long studentId;
    private Integer rating;
    private String comment;
    private LocalDateTime createTime;
    
    // 额外字段，用于显示
    private String studentName;
    private String studentSchool;
}
