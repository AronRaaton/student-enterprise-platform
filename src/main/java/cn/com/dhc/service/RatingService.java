package cn.com.dhc.service;

import cn.com.dhc.Entity.EnterpriseRating;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.TaskRating;
import cn.com.dhc.dto.RatingDTO;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.repository.EnterpriseRatingRepository;
import cn.com.dhc.repository.TaskRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final TaskRatingRepository taskRatingRepository;
    private final EnterpriseRatingRepository enterpriseRatingRepository;
    private final StudentRepository studentRepository;

    // 任务评分相关方法
    @Transactional
    public RatingDTO rateTask(Long taskId, Long studentId, Integer rating, String comment) {
        Optional<TaskRating> existingRating = taskRatingRepository.findByTaskIdAndStudentId(taskId, studentId);
        
        TaskRating taskRating;
        if (existingRating.isPresent()) {
            // 更新现有评分
            taskRating = existingRating.get();
            taskRating.setRating(rating);
            taskRating.setComment(comment);
            taskRating.setCreateTime(LocalDateTime.now());
        } else {
            // 创建新评分
            taskRating = new TaskRating();
            taskRating.setTaskId(taskId);
            taskRating.setStudentId(studentId);
            taskRating.setRating(rating);
            taskRating.setComment(comment);
        }
        
        TaskRating savedRating = taskRatingRepository.save(taskRating);
        return convertTaskRatingToDTO(savedRating);
    }
    
    public List<RatingDTO> getTaskRatings(Long taskId) {
        List<TaskRating> ratings = taskRatingRepository.findByTaskId(taskId);
        return ratings.stream()
                .map(this::convertTaskRatingToDTO)
                .collect(Collectors.toList());
    }
    
    public RatingDTO getStudentTaskRating(Long taskId, Long studentId) {
        Optional<TaskRating> ratingOpt = taskRatingRepository.findByTaskIdAndStudentId(taskId, studentId);
        return ratingOpt.map(this::convertTaskRatingToDTO).orElse(null);
    }
    
    public Double getTaskAverageRating(Long taskId) {
        return taskRatingRepository.getAverageRatingByTaskId(taskId);
    }
    
    // 企业评分相关方法
    @Transactional
    public RatingDTO rateEnterprise(Long enterpriseId, Long studentId, Integer rating, String comment) {
        Optional<EnterpriseRating> existingRating = enterpriseRatingRepository.findByEnterpriseIdAndStudentId(enterpriseId, studentId);
        
        EnterpriseRating enterpriseRating;
        if (existingRating.isPresent()) {
            // 更新现有评分
            enterpriseRating = existingRating.get();
            enterpriseRating.setRating(rating);
            enterpriseRating.setComment(comment);
            enterpriseRating.setCreateTime(LocalDateTime.now());
        } else {
            // 创建新评分
            enterpriseRating = new EnterpriseRating();
            enterpriseRating.setEnterpriseId(enterpriseId);
            enterpriseRating.setStudentId(studentId);
            enterpriseRating.setRating(rating);
            enterpriseRating.setComment(comment);
        }
        
        EnterpriseRating savedRating = enterpriseRatingRepository.save(enterpriseRating);
        return convertEnterpriseRatingToDTO(savedRating);
    }
    
    public List<RatingDTO> getEnterpriseRatings(Long enterpriseId) {
        List<EnterpriseRating> ratings = enterpriseRatingRepository.findByEnterpriseId(enterpriseId);
        return ratings.stream()
                .map(this::convertEnterpriseRatingToDTO)
                .collect(Collectors.toList());
    }
    
    public RatingDTO getStudentEnterpriseRating(Long enterpriseId, Long studentId) {
        Optional<EnterpriseRating> ratingOpt = enterpriseRatingRepository.findByEnterpriseIdAndStudentId(enterpriseId, studentId);
        return ratingOpt.map(this::convertEnterpriseRatingToDTO).orElse(null);
    }
    
    public Double getEnterpriseAverageRating(Long enterpriseId) {
        return enterpriseRatingRepository.getAverageRatingByEnterpriseId(enterpriseId);
    }
    
    // 转换方法
    private RatingDTO convertTaskRatingToDTO(TaskRating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setTargetId(rating.getTaskId());
        dto.setStudentId(rating.getStudentId());
        dto.setRating(rating.getRating());
        dto.setComment(rating.getComment());
        dto.setCreateTime(rating.getCreateTime());
        
        // 获取学生信息
        studentRepository.findById(rating.getStudentId()).ifPresent(student -> {
            dto.setStudentName(student.getName());
            dto.setStudentSchool(student.getSchool());
        });
        
        return dto;
    }
    
    private RatingDTO convertEnterpriseRatingToDTO(EnterpriseRating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setTargetId(rating.getEnterpriseId());
        dto.setStudentId(rating.getStudentId());
        dto.setRating(rating.getRating());
        dto.setComment(rating.getComment());
        dto.setCreateTime(rating.getCreateTime());
        
        // 获取学生信息
        studentRepository.findById(rating.getStudentId()).ifPresent(student -> {
            dto.setStudentName(student.getName());
            dto.setStudentSchool(student.getSchool());
        });
        
        return dto;
    }
}
