package cn.com.dhc.controller;

import cn.com.dhc.dto.JobPostingDTO;
import cn.com.dhc.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprises")
public class EnterpriseJobController {

    @Autowired
    private JobPostingService jobPostingService;

    // 获取企业的所有岗位 - 为了兼容前端请求
    @GetMapping("/{enterpriseId}/jobs")
    public ResponseEntity<?> getEnterpriseJobs(@PathVariable Long enterpriseId) {
        try {
            System.out.println("学生查看企业岗位: enterpriseId=" + enterpriseId);
            List<JobPostingDTO> jobs = jobPostingService.getEnterpriseJobs(enterpriseId);
            System.out.println("获取到岗位数量: " + jobs.size());
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.out.println("获取企业岗位失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取企业岗位失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
