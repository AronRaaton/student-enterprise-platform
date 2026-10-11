package cn.com.dhc.controller;

import java.util.List;

import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.Task;
import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
	private final TaskService taskService;

	@PostMapping("/enterprise/{enterpriseId}")
	public ResponseEntity<Task> createTask(@PathVariable Long enterpriseId,
										   @RequestBody String content) {
		return ResponseEntity.ok(taskService.createTask(enterpriseId, content));
	}

	@DeleteMapping("/{taskId}/enterprise/{enterpriseId}")
	public ResponseEntity<Void> deleteTask(@PathVariable Long enterpriseId,
										   @PathVariable Long taskId) {
		taskService.deleteTask(enterpriseId, taskId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/all")
	public ResponseEntity<List<Task>> getAllTasks() {
		return ResponseEntity.ok(taskService.findAll());
	}

	@GetMapping("/{taskId}")
	public ResponseEntity<Task> getTask(@PathVariable Long taskId) {
		return ResponseEntity.ok(taskService.findByTaskId(taskId));
	}

	@GetMapping("/enterprise/{enterpriseId}")
	public ResponseEntity<List<Task>> getEnterpriseTasks(@PathVariable Long enterpriseId) {
		return ResponseEntity.ok(taskService.getEnterpriseTasks(enterpriseId));
	}

	@GetMapping("/{taskId}/students")
	public ResponseEntity<List<Student>> getTaskStudents(@PathVariable Long taskId) {
		return ResponseEntity.ok(taskService.getTaskStudents(taskId));
	}

	@PostMapping("/{taskId}/assign/{studentId}")
	public ResponseEntity<TaskSubmission> claimTask(@PathVariable Long taskId,
											   @PathVariable Long studentId) {
		return ResponseEntity.ok(taskService.assignTask(taskId, studentId));
	}

	// 获取任务的接取人数
	@GetMapping("/{taskId}/assignment-count")
	public ResponseEntity<Long> getTaskAssignmentCount(@PathVariable Long taskId) {
		return ResponseEntity.ok(taskService.getTaskAssignmentCount(taskId));
	}

	// 获取学生接取的任务数
	@GetMapping("/students/{studentId}/assignment-count")
	public ResponseEntity<Long> getStudentAssignmentCount(@PathVariable Long studentId) {
		return ResponseEntity.ok(taskService.getStudentAssignmentCount(studentId));
	}

	// 获取任务的所有提交记录
	@GetMapping("/{taskId}/submissions")
	public ResponseEntity<List<TaskSubmission>> getTaskSubmissions(@PathVariable Long taskId) {
		return ResponseEntity.ok(taskService.getTaskSubmissions(taskId));
	}

	// 获取任务的提交统计信息
	@GetMapping("/{taskId}/stats")
	public ResponseEntity<TaskStatsResponse> getTaskStats(@PathVariable Long taskId) {
		return ResponseEntity.ok(taskService.getTaskStats(taskId));
	}

	// 任务统计响应类
	@lombok.Data
	public static class TaskStatsResponse {
		private Long totalCount;
		private Long submittedCount;
		private Long needsRevisionCount;
		private Long completedCount;

		public TaskStatsResponse(Long totalCount, Long submittedCount, Long needsRevisionCount, Long completedCount) {
			this.totalCount = totalCount;
			this.submittedCount = submittedCount;
			this.needsRevisionCount = needsRevisionCount;
			this.completedCount = completedCount;
		}
	}
}
