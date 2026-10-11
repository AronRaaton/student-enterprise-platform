package cn.com.dhc.mapper;

import cn.com.dhc.Entity.EnterpriseVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnterpriseVerificationRepository extends JpaRepository<EnterpriseVerification, Long> {
    
    List<EnterpriseVerification> findByVerificationStatus(EnterpriseVerification.VerificationStatus status);
    
    List<EnterpriseVerification> findByEnterpriseEnterpriseId(Long enterpriseId);
    
    EnterpriseVerification findTopByEnterpriseEnterpriseIdOrderBySubmitTimeDesc(Long enterpriseId);
}
