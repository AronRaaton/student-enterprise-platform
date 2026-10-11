package cn.com.dhc.mapper;

import cn.com.dhc.Entity.ProjectStepSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectStepSubmissionRepository extends JpaRepository<ProjectStepSubmission, Long> {

    // 根据项目ID和步骤查找所有提交
    List<ProjectStepSubmission> findByProject_ProjectIdAndStepNumber(Long projectId, Integer stepNumber);

    // 根据项目ID查找所有提交（用于甘特图）
    List<ProjectStepSubmission> findByProject_ProjectId(Long projectId);

    // 根据项目ID、学生ID和步骤查找提交（最新批次）
    Optional<ProjectStepSubmission> findFirstByProject_ProjectIdAndStudent_StudentIdAndStepNumberOrderByBatchNumberDesc(Long projectId, Long studentId, Integer stepNumber);

    // 根据项目ID、学生ID、步骤和批次号查找提交
    Optional<ProjectStepSubmission> findByProject_ProjectIdAndStudent_StudentIdAndStepNumberAndBatchNumber(Long projectId, Long studentId, Integer stepNumber, Integer batchNumber);

    // 根据项目ID和学生ID查找所有提交
    List<ProjectStepSubmission> findByProject_ProjectIdAndStudent_StudentId(Long projectId, Long studentId);

    // 根据项目ID查找所有提交

    // 根据学生ID查找所有提交
    List<ProjectStepSubmission> findByStudent_StudentId(Long studentId);

    // 根据项目ID、步骤和状态查找提交
    List<ProjectStepSubmission> findByProject_ProjectIdAndStepNumberAndStatus(Long projectId, Integer stepNumber, String status);

    // 统计项目某步骤的已通过提交数量
    long countByProject_ProjectIdAndStepNumberAndStatus(Long projectId, Integer stepNumber, String status);

    // 根据项目ID、学生ID和步骤查找所有批次的提交
    List<ProjectStepSubmission> findByProject_ProjectIdAndStudent_StudentIdAndStepNumberOrderByBatchNumberDesc(Long projectId, Long studentId, Integer stepNumber);

    // 获取学生在某个项目某个步骤的最大批次号
    @Query("SELECT MAX(s.batchNumber) FROM ProjectStepSubmission s WHERE s.project.projectId = :projectId AND s.student.studentId = :studentId AND s.stepNumber = :stepNumber")
    Integer findMaxBatchNumberByProjectIdAndStudentIdAndStepNumber(@Param("projectId") Long projectId, @Param("studentId") Long studentId, @Param("stepNumber") Integer stepNumber);
}
