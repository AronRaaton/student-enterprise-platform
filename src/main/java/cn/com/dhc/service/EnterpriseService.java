package cn.com.dhc.service;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.EnterpriseFavorite;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.dto.EnterpriseUpdateDTO;
import cn.com.dhc.mapper.EnterpriseFavoriteRepository;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.mapper.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseService {
    private final EnterpriseFavoriteRepository favoriteRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final StudentRepository studentRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;

    // 收藏学生
    @Transactional
    public void addToFavorites(Long enterpriseId, Long studentId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        if (!favoriteRepository.existsByEnterpriseAndStudent(enterprise, student)) {
            EnterpriseFavorite fav = new EnterpriseFavorite();
            fav.setEnterprise(enterprise);
            fav.setStudent(student);
            fav.setCreatedTime(LocalDateTime.now());
            favoriteRepository.save(fav);
        }
    }
    public Enterprise getEnterprise(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findByEnterpriseId(enterpriseId);
        if (enterprise == null) {
            throw new RuntimeException("企业不存在");
        }
        return enterprise;
    }

    // 获取企业相关的学生
    public List<Student> getEnterpriseStudents(Long enterpriseId) {
        // 获取企业信息
        Enterprise enterprise = getEnterprise(enterpriseId);

        // 获取已完成任务的学生
        List<Student> students = taskSubmissionRepository.findCompletedSubmissionsByEnterpriseId(enterpriseId)
                .stream()
                .map(submission -> submission.getStudent())
                .distinct()
                .collect(Collectors.toList());

        // 如果没有学生完成任务，返回收藏的学生
        if (students.isEmpty()) {
            students = getFavoriteStudents(enterpriseId);
        }

        return students;
    }
    // 取消收藏
    @Transactional
    public void removeFromFavorites(Long enterpriseId, Long studentId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        favoriteRepository.deleteByEnterpriseAndStudent(enterprise, student);
    }

    public List<Student> getFavoriteStudents(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));

        return favoriteRepository.findByEnterprise(enterprise)
                .stream()
                .map(EnterpriseFavorite::getStudent)
                .collect(Collectors.toList());
    }

    public Page<Enterprise> findAll(PageRequest of) {
        return enterpriseRepository.findAll(of);
    }

    public Enterprise updateEnterprise(Long id, EnterpriseUpdateDTO updateData) {
        Enterprise enterprise = enterpriseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enterprise not found with id: " + id));

        // 更新企业信息
        enterprise.setName(updateData.getName());
        enterprise.setEmail(updateData.getEmail());

        return enterpriseRepository.save(enterprise);
    }
}
