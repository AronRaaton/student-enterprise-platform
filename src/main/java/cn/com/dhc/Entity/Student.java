package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "student_id")  // 确保列名与TaskSubmission中的引用匹配
	private Long studentId;

	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;

	private String school;

	private Integer enrollmentYear;
	private String studentNumber;
	@Column(name = "name", nullable = false, length = 100) // 中文需要更长字段
	private String name;

	@Column(unique = true, length = 100)
	private String email;
	private Integer completedTasks = 0;

	@Column(name = "register_date")
	private LocalDateTime registerDate = LocalDateTime.now();

	@Column(name = "resume_file")
	private String resumeFile;

	@Column(name = "transcript_file")
	private String transcriptFile;

	@Column(name = "portfolio_files", columnDefinition = "TEXT")
	private String portfolioFiles;

	@Column(name = "major")
	private String major;

	@ManyToMany
	@JoinTable(name = "student_follow_enterprise",
			joinColumns = @JoinColumn(name = "student_id"),
			inverseJoinColumns = @JoinColumn(name = "enterprise_id"))
	@JsonIgnore
	private Set<Enterprise> followedEnterprises = new HashSet<>();

	@ManyToMany(mappedBy = "assignedStudents")
	@JsonIgnore
	private Set<Task> assignedTasks = new HashSet<>();


}