package cn.com.dhc.mapper;

import cn.com.dhc.Entity.StudentFavoriteEnterprise;
import cn.com.dhc.Entity.StudentFavoriteEnterpriseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentFavoriteEnterpriseRepository extends JpaRepository<StudentFavoriteEnterprise, StudentFavoriteEnterpriseId> {
    
    // 获取学生收藏的企业列表
    List<StudentFavoriteEnterprise> findByStudentId(Long studentId);
    
    // 检查学生是否已收藏企业
    boolean existsByStudentIdAndEnterpriseId(Long studentId, Long enterpriseId);
    
    // 统计学生收藏的企业数量
    @Query(value = "SELECT COUNT(*) FROM student_favorite_enterprise WHERE student_id = :studentId", nativeQuery = true)
    Integer countFavoriteEnterprisesByStudentId(@Param("studentId") Long studentId);
}
