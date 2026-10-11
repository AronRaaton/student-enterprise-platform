package cn.com.dhc.mapper;

import cn.com.dhc.Entity.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    
    // 查找企业发布的竞赛
    List<Competition> findByEnterpriseEnterpriseId(Long enterpriseId);
    
    // 分页查找所有竞赛
    Page<Competition> findAll(Pageable pageable);
    
    // 分页查找状态为开放的竞赛
    Page<Competition> findByStatus(String status, Pageable pageable);
    
    // 查找学生参与的竞赛
    @Query("SELECT c FROM Competition c JOIN c.registrations r WHERE r.student.studentId = :studentId")
    List<Competition> findCompetitionsByStudentId(Long studentId);
}
