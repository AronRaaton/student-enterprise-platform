package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "task_id")  // 确保列名与TaskSubmission中的引用匹配
	private Long taskId;

	@Column(nullable = false, length = 200)
	private String summary;  // 任务概要

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;  // 详细内容

	@Column(nullable = false, length = 200)
	private String techStack;  // 技术栈要求

	@Column(nullable = false)
	private Integer difficulty = 3;  // 任务难度，1-5星

	private LocalDateTime createTime = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "enterprise_id")
	private Enterprise enterprise;

	@OneToMany(mappedBy = "task")
	@JsonIgnore
	private List<TaskSubmission> submissions = new ArrayList<>();

	@ManyToMany
	@JoinTable(name = "task_assignment",
			joinColumns = @JoinColumn(name = "task_id"),
			inverseJoinColumns = @JoinColumn(name = "student_id"))
	@JsonIgnore
	private Set<Student> assignedStudents = new HashSet<>();

	@Column(nullable = false, length = 200)
	private String requirements;

	// 添加状态字段，默认为“招募中”
	@Column(length = 50)
	private String status = "OPEN";

}