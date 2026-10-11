package cn.com.dhc.controller;

import cn.com.dhc.dto.JobPostingDTO;
import cn.com.dhc.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingController {

    @Autowired
    private JobPostingService jobPostingService;

    // 创建新岗位
    @PostMapping
    public ResponseEntity<?> createJobPosting(@RequestBody JobPostingDTO jobPostingDTO) {
        try {
            JobPostingDTO createdJob = jobPostingService.createJobPosting(jobPostingDTO);
            return ResponseEntity.ok(createdJob);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "创建岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 更新岗位信息
    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateJobPosting(@PathVariable Long jobId, @RequestBody JobPostingDTO jobPostingDTO) {
        try {
            JobPostingDTO updatedJob = jobPostingService.updateJobPosting(jobId, jobPostingDTO);
            return ResponseEntity.ok(updatedJob);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 更改岗位状态
    @PutMapping("/{jobId}/status")
    public ResponseEntity<?> updateJobStatus(@PathVariable Long jobId, @RequestParam String status) {
        try {
            JobPostingDTO updatedJob = jobPostingService.updateJobStatus(jobId, status);
            return ResponseEntity.ok(updatedJob);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新岗位状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取岗位详情
    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJobPosting(@PathVariable Long jobId) {
        try {
            JobPostingDTO job = jobPostingService.getJobPosting(jobId);
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取岗位详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取企业的所有岗位
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<?> getEnterpriseJobs(@PathVariable Long enterpriseId) {
        try {
            List<JobPostingDTO> jobs = jobPostingService.getEnterpriseJobs(enterpriseId);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取企业岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取企业的开放岗位
    @GetMapping("/enterprise/{enterpriseId}/open")
    public ResponseEntity<?> getEnterpriseOpenJobs(@PathVariable Long enterpriseId) {
        try {
            List<JobPostingDTO> jobs = jobPostingService.getEnterpriseOpenJobs(enterpriseId);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取企业开放岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 分页获取所有开放岗位
    @GetMapping
    public ResponseEntity<?> getAllOpenJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<JobPostingDTO> jobPage = jobPostingService.getAllOpenJobs(page, size);
            return ResponseEntity.ok(jobPage);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取开放岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 根据城市和岗位类型筛选岗位
    @GetMapping("/filter")
    public ResponseEntity<?> filterJobs(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String jobType) {
        try {
            List<JobPostingDTO> jobs = jobPostingService.filterJobs(city, jobType);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "筛选岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 搜索岗位
    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<JobPostingDTO> jobPage = jobPostingService.searchJobs(keyword, page, size);
            return ResponseEntity.ok(jobPage);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "搜索岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 高级筛选岗位（根据薪资范围、城市、企业规模和岗位类型）
    @GetMapping("/advanced-filter")
    public ResponseEntity<?> advancedFilterJobs(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            @RequestParam(required = false) String companySize,
            @RequestParam(required = false) String jobType) {
        try {
            List<JobPostingDTO> jobs = jobPostingService.advancedFilterJobs(city, minSalary, maxSalary, companySize, jobType);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "高级筛选岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取相似岗位
    @GetMapping("/{jobId}/similar")
    public ResponseEntity<?> getSimilarJobs(@PathVariable Long jobId) {
        try {
            List<JobPostingDTO> similarJobs = jobPostingService.getSimilarJobs(jobId);
            return ResponseEntity.ok(similarJobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取相似岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
