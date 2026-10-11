package cn.com.dhc.repository;

import cn.com.dhc.Entity.TrainingChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingChapterRepository extends JpaRepository<TrainingChapter, Long> {
    
    List<TrainingChapter> findByMaterialIdOrderBySequenceAsc(Long materialId);
}
