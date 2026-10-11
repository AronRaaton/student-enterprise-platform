package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "task_submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(columnDefinition = "TEXT")
    private String submissionContent;

    private LocalDateTime submissionTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.NOT_SUBMITTED;
    private String fileUrl; // 外部链接，如GitHub链接

    private String uploadedFile; // 上传的文件名

    public void setContent(String content) {
        this.submissionContent = content;
    }

    public enum SubmissionStatus {
        NOT_SUBMITTED,   // 未提交
        SUBMITTED,      // 已提交待审核
        NEEDS_REVISION, // 需要重新提交
        COMPLETED       // 任务通过
    }

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Integer rating;

    private LocalDateTime reviewTime;

    private Integer submissionCount = 1;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "submission")
    private List<SubmissionHistory> histories = new ArrayList<>();

}



