package cn.com.dhc.service;

import cn.com.dhc.Entity.Student;
import cn.com.dhc.dto.StudentUpdateDTO;
import cn.com.dhc.mapper.StudentFollowEnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentFollowEnterpriseRepository studentFollowEnterpriseRepository;

    public Page<Student> findAll(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Student findById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public Student findByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void deleteById(Long studentId) {
        studentRepository.deleteById(studentId);
    }

    public Student updateStudent(Long id, StudentUpdateDTO updateData) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        // 更新学生信息
        student.setStudentNumber(updateData.getStudentNumber());
        student.setName(updateData.getName());
        student.setEmail(updateData.getEmail());
        student.setSchool(updateData.getSchool());
        student.setMajor(updateData.getMajor());
        student.setEnrollmentYear(updateData.getEnrollmentYear());

        return studentRepository.save(student);
    }

    // 获取学生关注的企业数量
    public Integer getFollowedEnterprisesCount(Long studentId) {
        return studentFollowEnterpriseRepository.countFollowedEnterprisesByStudentId(studentId);
    }
}