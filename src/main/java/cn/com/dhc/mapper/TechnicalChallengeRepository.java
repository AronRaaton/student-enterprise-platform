package cn.com.dhc.mapper;

import cn.com.dhc.Entity.TechnicalChallenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicalChallengeRepository extends JpaRepository<TechnicalChallenge, Long> {
    
    // 根据企业ID查找所有技术挑战
    List<TechnicalChallenge> findByEnterprise_EnterpriseId(Long enterpriseId);
    
    // 根据企业ID和状态查找技术挑战
    List<TechnicalChallenge> findByEnterprise_EnterpriseIdAndStatus(Long enterpriseId, String status);
    
    // 根据状态查找技术挑战
    List<TechnicalChallenge> findByStatus(String status);
    
    // 分页查询所有开放的技术挑战
    Page<TechnicalChallenge> findByStatus(String status, Pageable pageable);
}
