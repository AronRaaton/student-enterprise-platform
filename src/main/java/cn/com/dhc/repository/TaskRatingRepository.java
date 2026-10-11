package cn.com.dhc.repository;

import cn.com.dhc.Entity.TaskRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRatingRepository extends JpaRepository<TaskRating, Long> {
    
    List<TaskRating> findByTaskId(Long taskId);
    
    Optional<TaskRating> findByTaskIdAndStudentId(Long taskId, Long studentId);
    
    @Query("SELECT AVG(r.rating) FROM TaskRating r WHERE r.taskId = ?1")
    Double getAverageRatingByTaskId(Long taskId);
}
