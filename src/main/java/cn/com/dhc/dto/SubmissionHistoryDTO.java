package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionHistoryDTO {
    private Long historyId;
    private String content;
    private LocalDateTime createdTime;
    private String submissionType; // PARTIAL(部分完成)或COMPLETE(全部完成)
    private String description; // 本次提交描述
    private String fileUrl; // 外部文件链接
    private String uploadedFile; // 上传的文件名
    private String status; // SUBMITTED(已提交)、APPROVED(已通过)、NEEDS_REVISION(需修改)
    private String feedback; // 企业反馈
    private LocalDateTime reviewTime; // 审核时间
    private Long submissionId; // 关联的提交ID
    
    // 格式化后的时间，用于前端显示
    private String formattedCreatedTime;
    private String formattedReviewTime;
    
    // 显示名称
    private String submissionTypeDisplayName;
    private String statusDisplayName;
}
