package cn.com.dhc.mapper;

import cn.com.dhc.Entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    
    // 查找学生在特定活动的报名记录
    Optional<EventRegistration> findByEventEventIdAndStudentStudentId(Long eventId, Long studentId);
    
    // 查找特定活动的所有报名记录
    List<EventRegistration> findByEventEventId(Long eventId);
    
    // 查找学生的所有活动报名记录
    List<EventRegistration> findByStudentStudentId(Long studentId);
}
