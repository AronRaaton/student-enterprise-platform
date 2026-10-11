package cn.com.dhc.mapper;

import cn.com.dhc.Entity.CompetitionRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionRegistrationRepository extends JpaRepository<CompetitionRegistration, Long> {
    
    // 查找学生在特定竞赛的报名记录
    Optional<CompetitionRegistration> findByCompetitionCompetitionIdAndStudentStudentId(Long competitionId, Long studentId);
    
    // 查找特定竞赛的所有报名记录
    List<CompetitionRegistration> findByCompetitionCompetitionId(Long competitionId);
    
    // 查找学生的所有竞赛报名记录
    List<CompetitionRegistration> findByStudentStudentId(Long studentId);
}
