package cn.com.dhc.mapper;

import cn.com.dhc.Entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    
    // 根据岗位ID查找所有申请
    List<JobApplication> findByJob_JobId(Long jobId);
    
    // 根据岗位ID和状态查找申请
    List<JobApplication> findByJob_JobIdAndStatus(Long jobId, String status);
    
    // 根据学生ID查找所有申请
    List<JobApplication> findByStudent_StudentId(Long studentId);
    
    // 根据学生ID和状态查找申请
    List<JobApplication> findByStudent_StudentIdAndStatus(Long studentId, String status);
    
    // 根据岗位ID和学生ID查找申请
    Optional<JobApplication> findByJob_JobIdAndStudent_StudentId(Long jobId, Long studentId);
    
    // 统计岗位的申请数量
    long countByJob_JobId(Long jobId);
    
    // 统计岗位的已通过申请数量
    long countByJob_JobIdAndStatus(Long jobId, String status);
    
    // 查询企业的所有岗位申请
    @Query("SELECT a FROM JobApplication a WHERE a.job.enterprise.enterpriseId = :enterpriseId")
    List<JobApplication> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
    
    // 查询企业的待审核岗位申请
    @Query("SELECT a FROM JobApplication a WHERE a.job.enterprise.enterpriseId = :enterpriseId AND a.status = 'PENDING'")
    List<JobApplication> findPendingByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
}
