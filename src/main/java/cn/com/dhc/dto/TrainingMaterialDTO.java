package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMaterialDTO {
    private Long materialId;
    private Long enterpriseId;
    private String enterpriseName;
    private String title;
    private String description;
    private String industryType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String status;
    private List<TrainingChapterDTO> chapters = new ArrayList<>();
    
    // 格式化的日期时间
    private String formattedCreateTime;
    private String formattedUpdateTime;
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        if (createTime != null) {
            this.formattedCreateTime = createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        if (updateTime != null) {
            this.formattedUpdateTime = updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
