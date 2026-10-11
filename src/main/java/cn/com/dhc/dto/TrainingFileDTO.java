package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingFileDTO {
    private Long fileId;
    private Long chapterId;
    private String fileName;
    private String filePath;
    private String fileType;
    private LocalDateTime uploadTime;
    
    // 格式化的上传时间
    private String formattedUploadTime;
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
        if (uploadTime != null) {
            this.formattedUploadTime = uploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
