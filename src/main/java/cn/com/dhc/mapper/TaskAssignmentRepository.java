package cn.com.dhc.mapper;

import cn.com.dhc.Entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    // 统计某个任务的接取人数
    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.task.taskId = :taskId")
    Long countByTaskId(@Param("taskId") Long taskId);

    // 统计某个学生接取的任务数
    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.student.studentId = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    // 检查学生是否已经接取过某个任务
    boolean existsByTaskTaskIdAndStudentStudentId(Long taskId, Long studentId);
}