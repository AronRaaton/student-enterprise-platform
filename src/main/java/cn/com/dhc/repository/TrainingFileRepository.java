package cn.com.dhc.repository;

import cn.com.dhc.Entity.TrainingFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingFileRepository extends JpaRepository<TrainingFile, Long> {
    
    List<TrainingFile> findByChapterId(Long chapterId);
}
