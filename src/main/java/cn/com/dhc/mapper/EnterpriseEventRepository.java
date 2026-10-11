package cn.com.dhc.mapper;

import cn.com.dhc.Entity.EnterpriseEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnterpriseEventRepository extends JpaRepository<EnterpriseEvent, Long> {
    
    // 查找企业发布的活动
    List<EnterpriseEvent> findByEnterpriseEnterpriseId(Long enterpriseId);
    
    // 分页查找所有活动
    Page<EnterpriseEvent> findAll(Pageable pageable);
    
    // 分页查找状态为即将开始的活动
    Page<EnterpriseEvent> findByStatus(String status, Pageable pageable);
    
    // 查找学生参与的活动
    @Query("SELECT e FROM EnterpriseEvent e JOIN e.registrations r WHERE r.student.studentId = :studentId")
    List<EnterpriseEvent> findEventsByStudentId(Long studentId);
    
    // 按活动类型查找活动
    List<EnterpriseEvent> findByEventType(String eventType);
}
