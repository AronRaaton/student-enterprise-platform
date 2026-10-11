package cn.com.dhc.mapper;

import cn.com.dhc.Entity.StudentFollowEnterprise;
import cn.com.dhc.Entity.StudentFollowEnterpriseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentFollowEnterpriseRepository extends JpaRepository<StudentFollowEnterprise, StudentFollowEnterpriseId> {

    // 统计学生关注的企业数量
    @Query(value = "SELECT COUNT(*) FROM student_follow_enterprise WHERE student_id = :studentId", nativeQuery = true)
    Integer countFollowedEnterprisesByStudentId(@Param("studentId") Long studentId);
}
