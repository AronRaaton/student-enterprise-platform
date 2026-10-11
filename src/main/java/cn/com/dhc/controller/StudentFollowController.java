package cn.com.dhc.controller;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.StudentFollowEnterprise;
import cn.com.dhc.Entity.StudentFollowEnterpriseId;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentFollowEnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/students/follow")
@RequiredArgsConstructor
public class StudentFollowController {
    private final StudentRepository studentRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final StudentFollowEnterpriseRepository followRepository;

    // 学生关注企业
    @PostMapping("/{studentId}/enterprises/{enterpriseId}")
    public ResponseEntity<Void> followEnterprise(
            @PathVariable Long studentId,
            @PathVariable Long enterpriseId
    ) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        StudentFollowEnterpriseId id = new StudentFollowEnterpriseId();
        id.setStudentId(studentId);
        id.setEnterpriseId(enterpriseId);
        
        if (!followRepository.existsById(id)) {
            StudentFollowEnterprise follow = new StudentFollowEnterprise();
            follow.setId(id);
            follow.setStudentId(studentId);
            follow.setEnterpriseId(enterpriseId);
            followRepository.save(follow);
        }
        
        return ResponseEntity.ok().build();
    }
    
    // 学生取消关注企业
    @DeleteMapping("/{studentId}/enterprises/{enterpriseId}")
    public ResponseEntity<Void> unfollowEnterprise(
            @PathVariable Long studentId,
            @PathVariable Long enterpriseId
    ) {
        StudentFollowEnterpriseId id = new StudentFollowEnterpriseId();
        id.setStudentId(studentId);
        id.setEnterpriseId(enterpriseId);
        
        if (followRepository.existsById(id)) {
            followRepository.deleteById(id);
        }
        
        return ResponseEntity.ok().build();
    }
    
    // 检查学生是否关注了企业
    @GetMapping("/{studentId}/enterprises/{enterpriseId}/check")
    public ResponseEntity<Map<String, Boolean>> checkFollowing(
            @PathVariable Long studentId,
            @PathVariable Long enterpriseId
    ) {
        StudentFollowEnterpriseId id = new StudentFollowEnterpriseId();
        id.setStudentId(studentId);
        id.setEnterpriseId(enterpriseId);
        
        boolean isFollowing = followRepository.existsById(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        
        return ResponseEntity.ok(response);
    }
}
