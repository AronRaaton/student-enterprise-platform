package cn.com.dhc.controller;

import cn.com.dhc.dto.JobApplicationDTO;
import cn.com.dhc.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService applicationService;
    
    // 学生申请岗位
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> applyForJob(
            @PathVariable Long jobId,
            @RequestParam Long studentId) {
        try {
            JobApplicationDTO application = applicationService.applyForJob(jobId, studentId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "申请岗位失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // 企业审核岗位申请
    @PutMapping("/{applicationId}/review")
    public ResponseEntity<?> reviewApplication(
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String feedback = requestBody != null ? requestBody.get("feedback") : null;
            JobApplicationDTO application = applicationService.reviewApplication(applicationId, status, feedback);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "审核申请失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // 获取岗位的所有申请
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplicationDTO>> getJobApplications(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getJobApplications(jobId));
    }
    
    // 获取岗位的待审核申请
    @GetMapping("/job/{jobId}/pending")
    public ResponseEntity<List<JobApplicationDTO>> getPendingJobApplications(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getPendingJobApplications(jobId));
    }
    
    // 获取学生的所有申请
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<JobApplicationDTO>> getStudentApplications(@PathVariable Long studentId) {
        return ResponseEntity.ok(applicationService.getStudentApplications(studentId));
    }
    
    // 获取企业的所有岗位申请
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<List<JobApplicationDTO>> getEnterpriseApplications(@PathVariable Long enterpriseId) {
        return ResponseEntity.ok(applicationService.getEnterpriseApplications(enterpriseId));
    }
    
    // 获取企业的待审核岗位申请
    @GetMapping("/enterprise/{enterpriseId}/pending")
    public ResponseEntity<List<JobApplicationDTO>> getEnterprisePendingApplications(@PathVariable Long enterpriseId) {
        return ResponseEntity.ok(applicationService.getEnterprisePendingApplications(enterpriseId));
    }
    
    // 获取申请详情
    @GetMapping("/{applicationId}")
    public ResponseEntity<JobApplicationDTO> getApplicationById(@PathVariable Long applicationId) {
        return ResponseEntity.ok(applicationService.getApplicationById(applicationId));
    }
    
    // 检查学生是否已申请岗位
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> hasApplied(
            @RequestParam Long jobId,
            @RequestParam Long studentId) {
        boolean hasApplied = applicationService.hasApplied(jobId, studentId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasApplied", hasApplied);
        return ResponseEntity.ok(response);
    }
}
