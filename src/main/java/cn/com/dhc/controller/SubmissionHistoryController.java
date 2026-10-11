package cn.com.dhc.controller;

import cn.com.dhc.Entity.SubmissionHistory;
import cn.com.dhc.dto.SubmissionHistoryDTO;
import cn.com.dhc.service.TaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submission-history")
@RequiredArgsConstructor
public class SubmissionHistoryController {

    private final TaskService taskService;

    // 创建提交历史记录
    @PostMapping
    public ResponseEntity<SubmissionHistory> createSubmissionHistory(@RequestBody CreateHistoryRequest request) {
        return ResponseEntity.ok(taskService.createSubmissionHistory(
                request.getSubmissionId(),
                request.getContent(),
                request.getDescription(),
                request.getSubmissionType(),
                request.getFileUrl(),
                request.getUploadedFile()
        ));
    }

    // 获取提交历史记录列表
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<List<SubmissionHistoryDTO>> getSubmissionHistories(@PathVariable Long submissionId) {
        return ResponseEntity.ok(taskService.getSubmissionHistories(submissionId));
    }

    // 获取单个提交历史记录
    @GetMapping("/{historyId}")
    public ResponseEntity<SubmissionHistoryDTO> getSubmissionHistory(@PathVariable Long historyId) {
        return ResponseEntity.ok(taskService.getSubmissionHistory(historyId));
    }

    // 审核提交历史记录
    @PostMapping("/{historyId}/review")
    public ResponseEntity<SubmissionHistoryDTO> reviewSubmissionHistory(
            @PathVariable Long historyId,
            @RequestBody ReviewHistoryRequest request) {
        return ResponseEntity.ok(taskService.reviewSubmissionHistory(
                historyId,
                request.getStatus(),
                request.getFeedback()
        ));
    }

    @Data
    public static class CreateHistoryRequest {
        private Long submissionId;
        private String content;
        private String description;
        private String submissionType; // PARTIAL或COMPLETE
        private String fileUrl;
        private String uploadedFile;
    }

    @Data
    public static class ReviewHistoryRequest {
        private String status; // APPROVED或NEEDS_REVISION
        private String feedback;
    }
}
