package cn.com.dhc.mapper;

import cn.com.dhc.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TaskRepository extends JpaRepository<Task, Long> {
	@Query("SELECT t FROM Task t LEFT JOIN FETCH t.enterprise WHERE t.taskId = :taskId")
	Task findTaskWithEnterpriseById(@Param("taskId") Long taskId);


	List<Task> findByEnterprise_EnterpriseId(Long enterpriseId);

	// 获取学生接取的任务列表
	@Query("SELECT t FROM Task t JOIN TaskAssignment ta ON t.taskId = ta.task.taskId WHERE ta.student.studentId = :studentId")
	List<Task> findTasksByStudentId(@Param("studentId") Long studentId);
}
