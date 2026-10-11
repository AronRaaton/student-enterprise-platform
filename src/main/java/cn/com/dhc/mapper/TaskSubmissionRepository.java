package cn.com.dhc.mapper;

import cn.com.dhc.Entity.Task;
import cn.com.dhc.Entity.TaskSubmission;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    // 获取学生的所有任务提交
    List<TaskSubmission> findByStudent_StudentId(Long studentId);

    // 获取指定任务的提交记录
    List<TaskSubmission> findByTask_TaskId(Long taskId);

    // 检查学生是否已提交过某个任务
    boolean existsByTask_TaskIdAndStudent_StudentId(Long taskId, Long studentId);

    // 获取学生的特定任务的提交记录
    Optional<TaskSubmission> findByStudent_StudentIdAndTask_TaskId(Long studentId, Long taskId);

    // 获取学生接取的所有任务
    @Query("SELECT ts.task FROM TaskSubmission ts WHERE ts.student.studentId = :studentId")
    List<Task> findTasksByStudentId(@Param("studentId") Long studentId);

    // 获取学生最近5个任务
    @Query(value = "SELECT t.* FROM task t JOIN task_submission ts ON t.task_id = ts.task_id WHERE ts.student_id = :studentId ORDER BY ts.submission_time DESC LIMIT 5", nativeQuery = true)
    List<Task> findRecentTasksByStudent_StudentId(@Param("studentId") Long studentId);

    // 获取学生接取的任务数量
    @Query("SELECT COUNT(ts) FROM TaskSubmission ts WHERE ts.student.studentId = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    // 获取任务的接取人数
    @Query("SELECT COUNT(ts) FROM TaskSubmission ts WHERE ts.task.taskId = :taskId")
    Long countByTaskId(@Param("taskId") Long taskId);

    // 获取企业已完成任务的提交
    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.task.enterprise.enterpriseId = :enterpriseId AND ts.status = 'COMPLETED'")
    List<TaskSubmission> findCompletedSubmissionsByEnterpriseId(@Param("enterpriseId") Long enterpriseId);


}