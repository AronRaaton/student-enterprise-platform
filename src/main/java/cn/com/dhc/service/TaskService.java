package cn.com.dhc.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.com.dhc.Entity.*;
import cn.com.dhc.dto.SubmissionHistoryDTO;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.mapper.SubmissionHistoryRepository;
import cn.com.dhc.mapper.TaskRepository;
import cn.com.dhc.mapper.TaskSubmissionRepository;
import cn.com.dhc.mapper.TaskAssignmentRepository;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TaskService {
	private final TaskRepository taskRepository;
	private final EnterpriseRepository enterpriseRepository;
	private final StudentRepository studentRepository;
	private final TaskAssignmentRepository taskAssignmentRepository;
	private final TaskSubmissionRepository submissionRepository;
	private final SubmissionHistoryRepository submissionHistoryRepository;

	@Transactional
	public Task createTask(Long enterpriseId, String content) {
		Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
				.orElseThrow(() -> new RuntimeException("Enterprise not found"));
		Task task = new Task();
		task.setContent(content);
		task.setEnterprise(enterprise);
		task.setDifficulty(3); // 默认中等难度
		return taskRepository.save(task);
	}


	@Transactional
	public TaskSubmission assignTask(Long taskId, Long studentId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new RuntimeException("Student not found"));

		// 检查是否已经接取过该任务
		if (submissionRepository.existsByTask_TaskIdAndStudent_StudentId(taskId, studentId)) {
			throw new RuntimeException("已经接取过该任务");
		}

		// 创建任务提交记录（初始状态为未提交）
		TaskSubmission submission = new TaskSubmission();
		submission.setTask(task);
		submission.setStudent(student);
		submission.setStatus(TaskSubmission.SubmissionStatus.NOT_SUBMITTED);
		submission.setSubmissionTime(LocalDateTime.now());
		submissionRepository.save(submission);

		// 注意：不再在接取任务时增加学生的完成任务数
		// 完成任务数应该在任务审核通过后增加

		return submission;
	}

	public List<Task> getEnterpriseTasks(Long enterpriseId) {
		List<Task> tasks = taskRepository.findByEnterprise_EnterpriseId(enterpriseId);
		// 确保所有任务都有状态字段
		tasks.forEach(task -> {
			if (task.getStatus() == null) {
				task.setStatus("OPEN");
			}
		});
		return tasks;
	}

	public List<Student> getTaskStudents(Long taskId) {
		return taskRepository.findById(taskId)
				.map(task -> new ArrayList<>(task.getAssignedStudents()))
				.orElseThrow(() -> new RuntimeException("Task not found"));
	}

	// 创建任务（企业）
	@Transactional
	public Task createTask(Long enterpriseId, String summary, String content, String techStack, String requirements, Integer difficulty) {
		Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
				.orElseThrow(() -> new RuntimeException("企业不存在"));

		Task task = new Task();
		task.setSummary(summary);
		task.setContent(content);
		task.setTechStack(techStack);
		task.setRequirements(requirements);
		task.setDifficulty(difficulty);
		task.setEnterprise(enterprise);
		return taskRepository.save(task);
	}

	// 学生领取任务
	@Transactional
	public TaskSubmission claimTask(Long taskId, Long studentId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("任务不存在"));
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new RuntimeException("学生不存在"));

		// 检查是否已经领取
		if (submissionRepository.existsByTask_TaskIdAndStudent_StudentId(taskId, studentId)) {
			throw new RuntimeException("已领取该任务");
		}

		TaskSubmission submission = new TaskSubmission();
		submission.setTask(task);
		submission.setStudent(student);
		return submissionRepository.save(submission);
	}

	// 提交任务
	@Transactional
	public TaskSubmission submitTask(Long submissionId, Long studentId, String content) {
		TaskSubmission submission = submissionRepository.findById(submissionId)
				.filter(s -> s.getStudent().getStudentId().equals(studentId))
				.orElseThrow(() -> new RuntimeException("提交记录不存在"));

		submission.setSubmissionContent(content);
		submission.setStatus(TaskSubmission.SubmissionStatus.SUBMITTED);
		submission.setSubmissionTime(LocalDateTime.now());
		return submissionRepository.save(submission);
	}

	// 获取学生任务列表
	public List<Task> getStudentTasks(Long studentId) {
		// 使用TaskSubmission表获取学生接取的任务
		return submissionRepository.findTasksByStudentId(studentId);
	}

	// 获取学生的任务提交记录
	public List<TaskSubmission> getStudentSubmissions(Long studentId) {
		return submissionRepository.findByStudent_StudentId(studentId);
	}

	// 获取学生已完成的任务
	public List<TaskSubmission> getStudentCompletedTasks(Long studentId) {
		// 获取学生的所有提交记录
		List<TaskSubmission> allSubmissions = submissionRepository.findByStudent_StudentId(studentId);
		// 过滤出状态为 COMPLETED 的提交
		return allSubmissions.stream()
			.filter(submission -> submission.getStatus() == TaskSubmission.SubmissionStatus.COMPLETED)
			.collect(Collectors.toList());
	}

	// 获取学生的特定任务的提交记录
	public TaskSubmission getStudentTaskSubmission(Long studentId, Long taskId) {
		return submissionRepository.findByStudent_StudentIdAndTask_TaskId(studentId, taskId)
			.orElseThrow(() -> new RuntimeException("未找到提交记录"));
	}

	public List<TaskSubmission> getTaskSubmissions(Long taskId) {
		return submissionRepository.findByTask_TaskId(taskId);
	}

	@Transactional
	public TaskSubmission reviewSubmission(Long submissionId, int rating, String feedback) {
		TaskSubmission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new RuntimeException("提交记录不存在"));

		submission.setRating(rating);
		submission.setFeedback(feedback);
		submission.setReviewTime(LocalDateTime.now());
		submission.setStatus(TaskSubmission.SubmissionStatus.COMPLETED);

		return submissionRepository.save(submission);
	}

	// 学生修改提交
	@Transactional
	public TaskSubmission updateSubmission(Long submissionId, String newContent, Long studentId) {
		TaskSubmission submission = submissionRepository.findById(submissionId)
				.filter(s -> s.getStudent().getStudentId().equals(studentId))
				.filter(s -> s.getStatus() != TaskSubmission.SubmissionStatus.COMPLETED)
				.orElseThrow(() -> new RuntimeException("无法修改提交"));

		// 保存历史记录
		SubmissionHistory history = new SubmissionHistory();
		history.setContent(submission.getSubmissionContent());
		history.setCreatedTime(LocalDateTime.now());
		history.setSubmission(submission);
		submission.getHistories().add(history);

		// 更新内容
		submission.setSubmissionContent(newContent);
		submission.setSubmissionCount(submission.getSubmissionCount() + 1);
		submission.setStatus(TaskSubmission.SubmissionStatus.SUBMITTED);

		return submissionRepository.save(submission);
	}

	// 分页查询所有任务
	public Page<Task> findAll(Pageable pageable) {
		return taskRepository.findAll(pageable);
	}

	// 查询所有任务（不分页）
	public List<Task> findAll() {
		return taskRepository.findAll();
	}

	// 获取单个任务
	public Task findByTaskId(Long taskId) {
		return taskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));
	}

	// 获取任务的接取人数
	public Long getTaskAssignmentCount(Long taskId) {
		return submissionRepository.countByTaskId(taskId);
	}

	// 获取学生接取的任务数
	public Long getStudentAssignmentCount(Long studentId) {
		return submissionRepository.countByStudentId(studentId);
	}

	public Task getTaskById(Long taskId) {
		return taskRepository.findTaskWithEnterpriseById(taskId);
	}

	// 提交任务
	@Transactional
	public TaskSubmission submitTask(Long taskId, Long studentId, String content, String fileUrl) {
		// 获取学生的任务提交记录
		Optional<TaskSubmission> submissionOpt = submissionRepository.findByStudent_StudentIdAndTask_TaskId(studentId, taskId);

		if (!submissionOpt.isPresent()) {
			throw new RuntimeException("学生未接取该任务");
		}

		TaskSubmission submission = submissionOpt.get();

		// 更新提交内容
		submission.setSubmissionContent(content);
		// 只更新fileUrl，不更新uploadedFile，因为uploadedFile是通过文件上传接口设置的
		submission.setFileUrl(fileUrl);
		submission.setSubmissionTime(LocalDateTime.now());
		submission.setStatus(TaskSubmission.SubmissionStatus.SUBMITTED); // 更新状态为已提交待审核

		// 记录日志
		System.out.println("提交任务成功: taskId=" + taskId + ", studentId=" + studentId
			+ ", fileUrl=" + fileUrl + ", uploadedFile=" + submission.getUploadedFile());

		return submissionRepository.save(submission);
	}

	// 更新任务提交状态
	@Transactional
	public TaskSubmission updateSubmissionStatus(Long submissionId, TaskSubmission.SubmissionStatus status, String feedback, Integer rating) {
		TaskSubmission submission = submissionRepository.findById(submissionId)
			.orElseThrow(() -> new RuntimeException("未找到提交记录"));

		submission.setStatus(status);
		submission.setFeedback(feedback);
		submission.setReviewTime(LocalDateTime.now());

		// 设置评分
		if (rating != null) {
			submission.setRating(rating);
		}

		// 如果任务审核通过，增加学生的完成任务数
		if (status == TaskSubmission.SubmissionStatus.COMPLETED) {
			Student student = submission.getStudent();
			student.setCompletedTasks(student.getCompletedTasks() + 1);
			studentRepository.save(student);
		}

		return submissionRepository.save(submission);
	}

	// 兼容旧版本的方法
	@Transactional
	public TaskSubmission updateSubmissionStatus(Long submissionId, TaskSubmission.SubmissionStatus status, String feedback) {
		return updateSubmissionStatus(submissionId, status, feedback, null);
	}

	public Page<Task> findAllWithEnterprise(PageRequest pageRequest) {
		return taskRepository.findAll(pageRequest);  // 确保Task实体类配置了正确的企业关联
	}


	// 删除任务
	@Transactional
	public void deleteTask(Long enterpriseId, Long taskId) {
		Task task = taskRepository.findById(taskId)
			.orElseThrow(() -> new RuntimeException("任务不存在"));

		// 验证任务属于该企业
		if (!task.getEnterprise().getEnterpriseId().equals(enterpriseId)) {
			throw new RuntimeException("无权操作该任务");
		}

		// 删除任务相关的提交记录
		List<TaskSubmission> submissions = submissionRepository.findByTask_TaskId(taskId);
		submissionRepository.deleteAll(submissions);

		// 删除任务
		taskRepository.delete(task);
	}

	// 重新接取已完成的任务
	@Transactional
	public TaskSubmission retakeTask(Long taskId, Long studentId) {
		// 获取学生的任务提交记录
		TaskSubmission existingSubmission = submissionRepository.findByStudent_StudentIdAndTask_TaskId(studentId, taskId)
			.orElseThrow(() -> new RuntimeException("未找到提交记录"));

		// 如果任务已经完成，将状态改为未提交，并减少学生的已完成任务数
		if (existingSubmission.getStatus() == TaskSubmission.SubmissionStatus.COMPLETED) {
			// 减少学生的已完成任务数
			Student student = existingSubmission.getStudent();
			if (student.getCompletedTasks() > 0) {
				student.setCompletedTasks(student.getCompletedTasks() - 1);
				studentRepository.save(student);
			}

			// 修改现有提交记录的状态为未提交
			existingSubmission.setStatus(TaskSubmission.SubmissionStatus.NOT_SUBMITTED);
			existingSubmission.setSubmissionTime(LocalDateTime.now());
			existingSubmission.setSubmissionContent(""); // 清空提交内容
			existingSubmission.setFileUrl(""); // 清空外部链接
			existingSubmission.setUploadedFile(""); // 清空上传的文件

			// 设置提交次数
			int submissionCount = existingSubmission.getSubmissionCount() != null ?
				existingSubmission.getSubmissionCount() + 1 : 1;
			existingSubmission.setSubmissionCount(submissionCount);

			return submissionRepository.save(existingSubmission);
		} else {
			// 如果任务还没完成，返回现有提交记录
			return existingSubmission;
		}
	}

	// 获取任务的提交统计信息
	public cn.com.dhc.controller.TaskController.TaskStatsResponse getTaskStats(Long taskId) {
		// 获取任务的所有提交记录
		List<TaskSubmission> submissions = submissionRepository.findByTask_TaskId(taskId);

		// 统计各状态的数量
		Long totalCount = (long) submissions.size();
		Long submittedCount = submissions.stream()
			.filter(s -> s.getStatus() == TaskSubmission.SubmissionStatus.SUBMITTED)
			.count();
		Long needsRevisionCount = submissions.stream()
			.filter(s -> s.getStatus() == TaskSubmission.SubmissionStatus.NEEDS_REVISION)
			.count();
		Long completedCount = submissions.stream()
			.filter(s -> s.getStatus() == TaskSubmission.SubmissionStatus.COMPLETED)
			.count();

		return new cn.com.dhc.controller.TaskController.TaskStatsResponse(
			totalCount, submittedCount, needsRevisionCount, completedCount);
	}

	// 创建分批次提交记录
	@Transactional
	public SubmissionHistory createSubmissionHistory(Long submissionId, String content, String description,
			String submissionType, String fileUrl, String uploadedFile) {
		// 获取任务提交记录
		TaskSubmission submission = submissionRepository.findById(submissionId)
			.orElseThrow(() -> new RuntimeException("未找到提交记录"));

		// 创建历史记录
		SubmissionHistory history = new SubmissionHistory();
		history.setContent(content);
		history.setDescription(description);
		history.setSubmissionType(submissionType); // PARTIAL或COMPLETE
		history.setFileUrl(fileUrl);
		history.setUploadedFile(uploadedFile);
		history.setStatus("SUBMITTED"); // 初始状态为已提交
		history.setCreatedTime(LocalDateTime.now());
		history.setSubmission(submission);

		// 无论是部分完成还是全部完成，都更新主提交记录状态为待审核
		submission.setStatus(TaskSubmission.SubmissionStatus.SUBMITTED);
		submission.setSubmissionTime(LocalDateTime.now());

		// 如果是完成全部任务，还需要更新主提交记录的内容
		if ("COMPLETE".equals(submissionType)) {
			submission.setSubmissionContent(content);
			submission.setFileUrl(fileUrl);
			submission.setUploadedFile(uploadedFile);
		}

		// 保存主提交记录
		submissionRepository.save(submission);

		return submissionHistoryRepository.save(history);
	}

	// 获取提交历史记录列表
	public List<SubmissionHistoryDTO> getSubmissionHistories(Long submissionId) {
		// 按创建时间降序排序
		Sort sort = Sort.by(Sort.Direction.DESC, "createdTime");
		List<SubmissionHistory> histories = submissionHistoryRepository.findBySubmission_SubmissionId(submissionId, sort);

		// 转换为DTO
		return histories.stream().map(this::convertToHistoryDTO).collect(Collectors.toList());
	}

	// 获取单个提交历史记录
	public SubmissionHistoryDTO getSubmissionHistory(Long historyId) {
		SubmissionHistory history = submissionHistoryRepository.findById(historyId)
			.orElseThrow(() -> new RuntimeException("未找到历史记录"));

		return convertToHistoryDTO(history);
	}

	// 审核提交历史记录
	@Transactional
	public SubmissionHistoryDTO reviewSubmissionHistory(Long historyId, String status, String feedback) {
		SubmissionHistory history = submissionHistoryRepository.findById(historyId)
			.orElseThrow(() -> new RuntimeException("未找到历史记录"));

		// 更新状态和反馈
		history.setStatus(status); // APPROVED或NEEDS_REVISION
		history.setFeedback(feedback);
		history.setReviewTime(LocalDateTime.now());

		// 如果是完成全部任务的提交，并且审核通过，更新主提交记录状态
		if ("COMPLETE".equals(history.getSubmissionType()) && "APPROVED".equals(status)) {
			TaskSubmission submission = history.getSubmission();
			submission.setStatus(TaskSubmission.SubmissionStatus.COMPLETED);
			submission.setFeedback(feedback);
			submission.setReviewTime(LocalDateTime.now());

			// 增加学生的完成任务数
			Student student = submission.getStudent();
			student.setCompletedTasks(student.getCompletedTasks() + 1);
			studentRepository.save(student);

			submissionRepository.save(submission);
		} else if ("COMPLETE".equals(history.getSubmissionType()) && "NEEDS_REVISION".equals(status)) {
			// 如果是完成全部任务的提交，但需要修改，更新主提交记录状态
			TaskSubmission submission = history.getSubmission();
			submission.setStatus(TaskSubmission.SubmissionStatus.NEEDS_REVISION);
			submission.setFeedback(feedback);
			submission.setReviewTime(LocalDateTime.now());
			submissionRepository.save(submission);
		}

		return convertToHistoryDTO(submissionHistoryRepository.save(history));
	}

	// 将SubmissionHistory转换为DTO
	private SubmissionHistoryDTO convertToHistoryDTO(SubmissionHistory history) {
		SubmissionHistoryDTO dto = new SubmissionHistoryDTO();
		dto.setHistoryId(history.getHistoryId());
		dto.setContent(history.getContent());
		dto.setCreatedTime(history.getCreatedTime());
		dto.setSubmissionType(history.getSubmissionType());
		dto.setDescription(history.getDescription());
		dto.setFileUrl(history.getFileUrl());
		dto.setUploadedFile(history.getUploadedFile());
		dto.setStatus(history.getStatus());
		dto.setFeedback(history.getFeedback());
		dto.setReviewTime(history.getReviewTime());
		dto.setSubmissionId(history.getSubmission().getSubmissionId());

		// 格式化时间
		if (history.getCreatedTime() != null) {
			dto.setFormattedCreatedTime(history.getCreatedTime().toString().replace('T', ' '));
		}
		if (history.getReviewTime() != null) {
			dto.setFormattedReviewTime(history.getReviewTime().toString().replace('T', ' '));
		}

		// 设置显示名称
		if ("PARTIAL".equals(history.getSubmissionType())) {
			dto.setSubmissionTypeDisplayName("部分完成");
		} else if ("COMPLETE".equals(history.getSubmissionType())) {
			dto.setSubmissionTypeDisplayName("全部完成");
		}

		if ("SUBMITTED".equals(history.getStatus())) {
			dto.setStatusDisplayName("已提交");
		} else if ("APPROVED".equals(history.getStatus())) {
			dto.setStatusDisplayName("已通过");
		} else if ("NEEDS_REVISION".equals(history.getStatus())) {
			dto.setStatusDisplayName("需修改");
		}

		return dto;
	}
}
