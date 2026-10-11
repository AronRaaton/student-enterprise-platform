package cn.com.dhc.controller;

import cn.com.dhc.Entity.Task;
import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.service.TaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/tasks")
@RequiredArgsConstructor
public class StudentTaskController {
    private final TaskService taskService;

    // 领取任务
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<TaskSubmission> claimTask(
            @PathVariable Long taskId,
            @RequestParam Long studentId) {
        return ResponseEntity.ok(taskService.assignTask(taskId, studentId));
    }

    // 提交任务
    @PostMapping("/submissions/{studentId}/tasks/{taskId}")
    public ResponseEntity<TaskSubmission> submitTask(
            @PathVariable Long studentId,
            @PathVariable Long taskId,
            @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(taskService.submitTask(taskId, studentId, request.getContent(), request.getFileUrl()));
    }

    // 查看已领取任务
    @GetMapping("/{studentId}")
    public ResponseEntity<List<Task>> getStudentTasks(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(taskService.getStudentTasks(studentId));
    }

    // 获取学生的任务提交记录
    @GetMapping("/submissions/{studentId}")
    public ResponseEntity<List<TaskSubmission>> getStudentSubmissions(@PathVariable Long studentId) {
        return ResponseEntity.ok(taskService.getStudentSubmissions(studentId));
    }

    // 获取学生已完成的任务
    @GetMapping("/completed/{studentId}")
    public ResponseEntity<List<TaskSubmission>> getStudentCompletedTasks(@PathVariable Long studentId) {
        return ResponseEntity.ok(taskService.getStudentCompletedTasks(studentId));
    }

    // 获取学生的特定任务的提交记录
    @GetMapping("/submissions/{studentId}/tasks/{taskId}")
    public ResponseEntity<TaskSubmission> getStudentTaskSubmission(
            @PathVariable Long studentId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getStudentTaskSubmission(studentId, taskId));
    }

    // 重新接取已完成的任务
    @PostMapping("/{taskId}/retake/{studentId}")
    public ResponseEntity<TaskSubmission> retakeTask(
            @PathVariable Long taskId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(taskService.retakeTask(taskId, studentId));
    }

    @Data
    public static class SubmissionRequest {
        private String content;
        private String fileUrl; // 外部链接，如GitHub链接
    }
}