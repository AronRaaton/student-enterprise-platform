package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Data
public class SubmissionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Lob
    private String content;

    private LocalDateTime createdTime;

    @Column(name = "submission_type")
    private String submissionType = "PARTIAL"; // PARTIAL(部分完成)或COMPLETE(全部完成)

    @Column(columnDefinition = "TEXT")
    private String description; // 本次提交描述

    @Column(name = "file_url")
    private String fileUrl; // 外部文件链接

    @Column(name = "uploaded_file")
    private String uploadedFile; // 上传的文件名

    @Column(name = "status")
    private String status = "SUBMITTED"; // SUBMITTED(已提交)、APPROVED(已通过)、NEEDS_REVISION(需修改)

    @Column(columnDefinition = "TEXT")
    private String feedback; // 企业反馈

    @Column(name = "review_time")
    private LocalDateTime reviewTime; // 审核时间

    @ManyToOne
    @JoinColumn(name = "submission_id")
    @JsonIgnore
    private TaskSubmission submission;

    // 提交类型枚举
    public enum SubmissionType {
        PARTIAL("部分完成"),
        COMPLETE("全部完成");

        private final String displayName;

        SubmissionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 提交状态枚举
    public enum HistoryStatus {
        SUBMITTED("已提交"),
        APPROVED("已通过"),
        NEEDS_REVISION("需修改");

        private final String displayName;

        HistoryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}