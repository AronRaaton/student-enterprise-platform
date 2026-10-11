package cn.com.dhc.repository;

import cn.com.dhc.Entity.TrainingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingReportRepository extends JpaRepository<TrainingReport, Long> {
    List<TrainingReport> findByProjectId(Long projectId);
    Optional<TrainingReport> findByProjectIdAndStudentId(Long projectId, Long studentId);
}
