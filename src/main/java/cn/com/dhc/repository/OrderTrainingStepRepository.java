package cn.com.dhc.repository;

import cn.com.dhc.Entity.OrderTrainingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderTrainingStepRepository extends JpaRepository<OrderTrainingStep, Long> {
    Optional<OrderTrainingStep> findByProjectIdAndStepNumber(Long projectId, Integer stepNumber);
}
