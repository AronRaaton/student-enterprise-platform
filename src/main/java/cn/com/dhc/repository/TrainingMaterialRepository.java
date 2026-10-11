package cn.com.dhc.repository;

import cn.com.dhc.Entity.TrainingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, Long> {
    
    List<TrainingMaterial> findByEnterpriseIdAndStatus(Long enterpriseId, String status);
    
    List<TrainingMaterial> findByIndustryTypeAndStatus(String industryType, String status);
    
    @Query("SELECT DISTINCT t.industryType FROM TrainingMaterial t WHERE t.status = 'ACTIVE'")
    List<String> findAllIndustryTypes();
    
    List<TrainingMaterial> findByStatus(String status);
    
    @Query("SELECT t FROM TrainingMaterial t WHERE t.status = 'ACTIVE' AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TrainingMaterial> searchByKeyword(String keyword);
}
