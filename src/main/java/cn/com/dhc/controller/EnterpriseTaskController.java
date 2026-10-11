package cn.com.dhc.controller;

import cn.com.dhc.Entity.Task;
import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.service.TaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cn.com.dhc.dto.TaskCreateRequest;


import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/enterprises/tasks")
@RequiredArgsConstructor
public class EnterpriseTaskController {

    private final TaskService taskService;

    // 创建任务
    @PostMapping("/{enterpriseId}")
    public ResponseEntity<Task> createTask(
            @PathVariable Long enterpriseId,
            @RequestBody TaskCreateRequest request) {
        return ResponseEntity.ok(taskService.createTask(
                enterpriseId,
                request.getSummary(),
                request.getContent(),
                request.getTechStack(),
                request.getRequirements(),
                request.getDifficulty()));
    }

    // 查看任务提交情况
    @GetMapping("/{taskId}/submissions")
    public ResponseEntity<List<TaskSubmission>> getTaskSubmissions(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskSubmissions(taskId));
    }

    @PostMapping("/submissions/{submissionId}/review")
    public ResponseEntity<TaskSubmission> reviewSubmission(
            @PathVariable Long submissionId,
            @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(taskService.updateSubmissionStatus(
                submissionId,
                request.getStatus(),
                request.getFeedback(),
                request.getRating()));
    }

    @DeleteMapping("/{enterpriseId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long enterpriseId,
            @PathVariable Long taskId) {
        taskService.deleteTask(enterpriseId, taskId);
        return ResponseEntity.noContent().build();
    }

    // 获取企业的所有任务 - 为了兼容前端请求
    @GetMapping("/{enterpriseId}")
    public ResponseEntity<List<Task>> getEnterpriseTasks(
            @PathVariable Long enterpriseId) {
        return ResponseEntity.ok(taskService.getEnterpriseTasks(enterpriseId));
    }

    // 获取企业的所有任务 - 为了兼容前端请求（学生查看企业详情）
    @GetMapping("/tasks/{enterpriseId}")
    public ResponseEntity<List<Task>> getEnterpriseTasksForStudent(
            @PathVariable Long enterpriseId) {
        System.out.println("学生查看企业任务: enterpriseId=" + enterpriseId);
        List<Task> tasks = taskService.getEnterpriseTasks(enterpriseId);
        System.out.println("获取到任务数量: " + tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @Data
    public static class ReviewRequest {
        private TaskSubmission.SubmissionStatus status;
        private String feedback;
        private Integer rating;
    }

}

