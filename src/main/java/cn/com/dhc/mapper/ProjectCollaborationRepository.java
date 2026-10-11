package cn.com.dhc.mapper;

import cn.com.dhc.Entity.ProjectCollaboration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCollaborationRepository extends JpaRepository<ProjectCollaboration, Long> {
    
    // 根据企业ID查找所有项目
    List<ProjectCollaboration> findByEnterprise_EnterpriseId(Long enterpriseId);
    
    // 根据企业ID和状态查找项目
    List<ProjectCollaboration> findByEnterprise_EnterpriseIdAndStatus(Long enterpriseId, String status);
    
    // 根据状态查找项目
    List<ProjectCollaboration> findByStatus(String status);
    
    // 分页查询所有开放的项目
    Page<ProjectCollaboration> findByStatus(String status, Pageable pageable);
    
    // 搜索项目（标题、描述、方向、技能中包含关键词）
    @Query("SELECT p FROM ProjectCollaboration p WHERE p.status = :status AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.projectDirection) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProjectCollaboration> searchProjects(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
    
    // 根据项目方向查找项目
    List<ProjectCollaboration> findByProjectDirectionAndStatus(String projectDirection, String status);
    
    // 统计企业的项目数量
    long countByEnterprise_EnterpriseId(Long enterpriseId);
    
    // 统计企业的开放项目数量
    long countByEnterprise_EnterpriseIdAndStatus(Long enterpriseId, String status);
}
