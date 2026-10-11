package cn.com.dhc.mapper;

import cn.com.dhc.Entity.CompetitionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionSubmissionRepository extends JpaRepository<CompetitionSubmission, Long> {
    
    // 查找特定报名记录的所有提交
    List<CompetitionSubmission> findByRegistrationRegistrationId(Long registrationId);
    
    // 查找特定报名记录的最新提交
    CompetitionSubmission findTopByRegistrationRegistrationIdOrderBySubmissionTimeDesc(Long registrationId);
}
