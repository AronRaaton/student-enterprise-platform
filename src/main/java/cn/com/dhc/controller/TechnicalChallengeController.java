package cn.com.dhc.controller;

import cn.com.dhc.dto.TechnicalChallengeDTO;
import cn.com.dhc.dto.TechnicalChallengeSubmissionDTO;
import cn.com.dhc.service.TechnicalChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/challenges")
public class TechnicalChallengeController {

    @Autowired
    private TechnicalChallengeService challengeService;
    
    // 创建新技术挑战
    @PostMapping
    public ResponseEntity<?> createChallenge(@RequestBody TechnicalChallengeDTO challengeDTO) {
        try {
            TechnicalChallengeDTO createdChallenge = challengeService.createChallenge(challengeDTO);
            return ResponseEntity.ok(createdChallenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "创建技术挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 获取技术挑战详情
    @GetMapping("/{challengeId}")
    public ResponseEntity<?> getChallengeById(@PathVariable Long challengeId) {
        try {
            TechnicalChallengeDTO challenge = challengeService.getChallengeById(challengeId);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取技术挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 更新技术挑战状态
    @PutMapping("/{challengeId}/status")
    public ResponseEntity<?> updateChallengeStatus(
            @PathVariable Long challengeId,
            @RequestParam String status) {
        try {
            TechnicalChallengeDTO challenge = challengeService.updateChallengeStatus(challengeId, status);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新技术挑战状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 获取企业的所有技术挑战
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<?> getEnterpriseChallenges(@PathVariable Long enterpriseId) {
        try {
            List<TechnicalChallengeDTO> challenges = challengeService.getEnterpriseChallenges(enterpriseId);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取企业技术挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 获取企业的开放技术挑战
    @GetMapping("/enterprise/{enterpriseId}/open")
    public ResponseEntity<?> getEnterpriseOpenChallenges(@PathVariable Long enterpriseId) {
        try {
            List<TechnicalChallengeDTO> challenges = challengeService.getEnterpriseOpenChallenges(enterpriseId);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取企业开放技术挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 分页获取所有开放技术挑战
    @GetMapping
    public ResponseEntity<?> getAllOpenChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TechnicalChallengeDTO> challengePage = challengeService.getAllOpenChallenges(page, size);
            return ResponseEntity.ok(challengePage);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取开放技术挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 提交技术挑战解决方案
    @PostMapping("/{challengeId}/submit")
    public ResponseEntity<?> submitSolution(
            @PathVariable Long challengeId,
            @RequestParam Long studentId,
            @RequestParam String solutionDescription,
            @RequestParam(required = false) String solutionUrl,
            @RequestParam(required = false) MultipartFile file) {
        try {
            String uploadedFileName = null;
            
            // 处理文件上传
            if (file != null && !file.isEmpty()) {
                try {
                    // 生成唯一文件名
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFileName = UUID.randomUUID().toString() + extension;
                    
                    // 确保上传目录存在
                    Path uploadDir = Paths.get("uploads");
                    if (!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }
                    
                    // 保存文件
                    Path filePath = uploadDir.resolve(newFileName);
                    Files.copy(file.getInputStream(), filePath);
                    
                    uploadedFileName = newFileName;
                } catch (IOException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "文件上传失败: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            }
            
            TechnicalChallengeSubmissionDTO submission = challengeService.submitSolution(
                    challengeId, studentId, solutionDescription, solutionUrl, uploadedFileName);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "提交解决方案失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 审核技术挑战提交
    @PutMapping("/submissions/{submissionId}/review")
    public ResponseEntity<?> reviewSubmission(
            @PathVariable Long submissionId,
            @RequestParam String status,
            @RequestParam(required = false) String feedback,
            @RequestParam(required = false) Integer rating) {
        try {
            TechnicalChallengeSubmissionDTO submission = challengeService.reviewSubmission(
                    submissionId, status, feedback, rating);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "审核提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 获取技术挑战的所有提交
    @GetMapping("/{challengeId}/submissions")
    public ResponseEntity<?> getChallengeSubmissions(@PathVariable Long challengeId) {
        try {
            List<TechnicalChallengeSubmissionDTO> submissions = challengeService.getChallengeSubmissions(challengeId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取技术挑战提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 获取技术挑战的待审核提交
    @GetMapping("/{challengeId}/submissions/pending")
    public ResponseEntity<?> getPendingSubmissions(@PathVariable Long challengeId) {
        try {
            List<TechnicalChallengeSubmissionDTO> submissions = challengeService.getPendingSubmissions(challengeId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取待审核提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 获取学生的所有提交
    @GetMapping("/submissions/student/{studentId}")
    public ResponseEntity<?> getStudentSubmissions(@PathVariable Long studentId) {
        try {
            List<TechnicalChallengeSubmissionDTO> submissions = challengeService.getStudentSubmissions(studentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 获取学生已完成的技术挑战
    @GetMapping("/student/{studentId}/completed")
    public ResponseEntity<?> getStudentCompletedChallenges(@PathVariable Long studentId) {
        try {
            List<TechnicalChallengeSubmissionDTO> submissions = challengeService.getStudentCompletedChallenges(studentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生已完成技术挑战失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
