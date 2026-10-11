package cn.com.dhc.controller;

import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.mapper.TaskSubmissionRepository;
import cn.com.dhc.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enterprise-students") // Changed the base path to avoid conflict
@RequiredArgsConstructor
public class EnterpriseStudentController {
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final EnterpriseService enterpriseService;

    @GetMapping("/{enterpriseId}")
    public ResponseEntity<List<Student>> getEnterpriseStudents(@PathVariable Long enterpriseId) {
        try {
            // 获取已完成任务的学生
            List<TaskSubmission> completedSubmissions = taskSubmissionRepository.findCompletedSubmissionsByEnterpriseId(enterpriseId);
            
            if (completedSubmissions.isEmpty()) {
                // 如果没有已完成任务的学生，返回收藏的学生
                return ResponseEntity.ok(enterpriseService.getFavoriteStudents(enterpriseId));
            }
            
            // 提取学生并去重
            List<Student> students = completedSubmissions.stream()
                    .map(TaskSubmission::getStudent)
                    .distinct()
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>()); // 返回空列表而不是错误
        }
    }
}
