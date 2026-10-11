package cn.com.dhc.mapper;

import cn.com.dhc.Entity.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    
    // 根据项目ID查找所有申请
    List<ProjectApplication> findByProject_ProjectId(Long projectId);
    
    // 根据项目ID和状态查找申请
    List<ProjectApplication> findByProject_ProjectIdAndStatus(Long projectId, String status);
    
    // 根据学生ID查找所有申请
    List<ProjectApplication> findByStudent_StudentId(Long studentId);
    
    // 根据学生ID和状态查找申请
    List<ProjectApplication> findByStudent_StudentIdAndStatus(Long studentId, String status);
    
    // 根据项目ID和学生ID查找申请
    Optional<ProjectApplication> findByProject_ProjectIdAndStudent_StudentId(Long projectId, Long studentId);
    
    // 统计项目的申请数量
    long countByProject_ProjectId(Long projectId);
    
    // 统计项目的已通过申请数量
    long countByProject_ProjectIdAndStatus(Long projectId, String status);
    
    // 统计学生的已通过申请数量
    long countByStudent_StudentIdAndStatus(Long studentId, String status);
    
    // 查询学生参与的所有项目ID
    @Query("SELECT a.project.projectId FROM ProjectApplication a WHERE a.student.studentId = :studentId AND a.status = 'APPROVED'")
    List<Long> findApprovedProjectIdsByStudentId(@Param("studentId") Long studentId);
}
