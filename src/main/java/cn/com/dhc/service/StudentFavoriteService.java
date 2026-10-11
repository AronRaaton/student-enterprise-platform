package cn.com.dhc.service;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.StudentFavoriteEnterprise;
import cn.com.dhc.Entity.StudentFavoriteEnterpriseId;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentFavoriteEnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentFavoriteService {
    private final StudentRepository studentRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final StudentFavoriteEnterpriseRepository studentFavoriteEnterpriseRepository;

    // 获取学生收藏的企业列表
    public List<Enterprise> getFavoriteEnterprises(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        return studentFavoriteEnterpriseRepository.findByStudentId(studentId)
                .stream()
                .map(favorite -> enterpriseRepository.findById(favorite.getEnterpriseId())
                        .orElse(null))
                .filter(enterprise -> enterprise != null)
                .collect(Collectors.toList());
    }

    // 学生收藏企业
    @Transactional
    public void addFavoriteEnterprise(Long studentId, Long enterpriseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        StudentFavoriteEnterpriseId id = new StudentFavoriteEnterpriseId();
        id.setStudentId(studentId);
        id.setEnterpriseId(enterpriseId);
        
        if (!studentFavoriteEnterpriseRepository.existsById(id)) {
            StudentFavoriteEnterprise favorite = new StudentFavoriteEnterprise();
            favorite.setId(id);
            favorite.setStudentId(studentId);
            favorite.setEnterpriseId(enterpriseId);
            studentFavoriteEnterpriseRepository.save(favorite);
        }
    }

    // 学生取消收藏企业
    @Transactional
    public void removeFavoriteEnterprise(Long studentId, Long enterpriseId) {
        StudentFavoriteEnterpriseId id = new StudentFavoriteEnterpriseId();
        id.setStudentId(studentId);
        id.setEnterpriseId(enterpriseId);
        
        if (studentFavoriteEnterpriseRepository.existsById(id)) {
            studentFavoriteEnterpriseRepository.deleteById(id);
        }
    }
}
