package cn.com.dhc.mapper;

import cn.com.dhc.Entity.TechnicalChallengeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicalChallengeSubmissionRepository extends JpaRepository<TechnicalChallengeSubmission, Long> {
    
    // 根据挑战ID查找所有提交
    List<TechnicalChallengeSubmission> findByChallenge_ChallengeId(Long challengeId);
    
    // 根据挑战ID和状态查找提交
    List<TechnicalChallengeSubmission> findByChallenge_ChallengeIdAndStatus(Long challengeId, String status);
    
    // 根据学生ID查找所有提交
    List<TechnicalChallengeSubmission> findByStudent_StudentId(Long studentId);
    
    // 根据学生ID和状态查找提交
    List<TechnicalChallengeSubmission> findByStudent_StudentIdAndStatus(Long studentId, String status);
    
    // 根据挑战ID和学生ID查找提交
    Optional<TechnicalChallengeSubmission> findByChallenge_ChallengeIdAndStudent_StudentId(Long challengeId, Long studentId);
    
    // 统计挑战的提交数量
    long countByChallenge_ChallengeId(Long challengeId);
    
    // 统计挑战的已通过提交数量
    long countByChallenge_ChallengeIdAndStatus(Long challengeId, String status);
    
    // 统计学生的已通过提交数量
    long countByStudent_StudentIdAndStatus(Long studentId, String status);
    
    // 查询学生已完成的所有挑战ID
    @Query("SELECT s.challenge.challengeId FROM TechnicalChallengeSubmission s WHERE s.student.studentId = :studentId AND s.status = 'APPROVED'")
    List<Long> findApprovedChallengeIdsByStudentId(@Param("studentId") Long studentId);
    
    // 查询学生已完成的所有挑战提交
    @Query("SELECT s FROM TechnicalChallengeSubmission s WHERE s.student.studentId = :studentId AND s.status = 'APPROVED'")
    List<TechnicalChallengeSubmission> findApprovedSubmissionsByStudentId(@Param("studentId") Long studentId);
}
