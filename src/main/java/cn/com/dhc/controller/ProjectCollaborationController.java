package cn.com.dhc.controller;

import cn.com.dhc.dto.ProjectApplicationDTO;
import cn.com.dhc.dto.ProjectCollaborationDTO;
import cn.com.dhc.dto.ProjectStepSubmissionDTO;
import cn.com.dhc.dto.TrainingReportDTO;
import cn.com.dhc.dto.OrderTrainingStepDTO;
import cn.com.dhc.service.ProjectCollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectCollaborationController {

    @Autowired
    private ProjectCollaborationService projectService;

    // 创建新项目
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectCollaborationDTO projectDTO) {
        try {
            ProjectCollaborationDTO createdProject = projectService.createProject(projectDTO);
            return ResponseEntity.ok(createdProject);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "创建项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 更新项目信息
    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable Long projectId, @RequestBody ProjectCollaborationDTO projectDTO) {
        try {
            ProjectCollaborationDTO updatedProject = projectService.updateProject(projectId, projectDTO);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 更改项目状态
    @PutMapping("/{projectId}/status")
    public ResponseEntity<?> updateProjectStatus(@PathVariable Long projectId, @RequestParam String status) {
        try {
            ProjectCollaborationDTO updatedProject = projectService.updateProjectStatus(projectId, status);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新项目状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取项目详情
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable Long projectId) {
        try {
            ProjectCollaborationDTO project = projectService.getProject(projectId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取项目详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取企业的所有项目
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<?> getEnterpriseProjects(@PathVariable Long enterpriseId) {
        try {
            List<ProjectCollaborationDTO> projects = projectService.getEnterpriseProjects(enterpriseId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取企业项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 删除项目
    @DeleteMapping("/enterprise/{enterpriseId}/project/{projectId}")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long enterpriseId,
            @PathVariable Long projectId) {
        try {
            projectService.deleteProject(enterpriseId, projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "删除项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取企业的开放项目
    @GetMapping("/enterprise/{enterpriseId}/open")
    public ResponseEntity<?> getEnterpriseOpenProjects(@PathVariable Long enterpriseId) {
        try {
            List<ProjectCollaborationDTO> projects = projectService.getEnterpriseOpenProjects(enterpriseId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取企业开放项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 分页获取所有开放项目
    @GetMapping
    public ResponseEntity<?> getAllOpenProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ProjectCollaborationDTO> projectPage = projectService.getAllOpenProjects(page, size);
            return ResponseEntity.ok(projectPage);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取开放项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 根据项目方向筛选项目
    @GetMapping("/filter")
    public ResponseEntity<?> filterProjects(@RequestParam String direction) {
        try {
            List<ProjectCollaborationDTO> projects = projectService.filterProjectsByDirection(direction);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "筛选项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 搜索项目
    @GetMapping("/search")
    public ResponseEntity<?> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ProjectCollaborationDTO> projectPage = projectService.searchProjects(keyword, page, size);
            return ResponseEntity.ok(projectPage);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "搜索项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 学生申请参与项目
    @PostMapping("/{projectId}/apply")
    public ResponseEntity<?> applyForProject(
            @PathVariable Long projectId,
            @RequestParam Long studentId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String applicationReason = requestBody.get("applicationReason");
            String uploadedFile = requestBody.get("uploadedFile");
            String notes = requestBody.get("notes");
            String relevantExperience = requestBody.get("relevantExperience");
            String contactInfo = requestBody.get("contactInfo");

            ProjectApplicationDTO application = projectService.applyForProject(
                projectId, studentId, applicationReason, uploadedFile, notes, relevantExperience, contactInfo);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "申请项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 企业审核学生申请
    @PutMapping("/applications/{applicationId}/review")
    public ResponseEntity<?> reviewApplication(
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestBody Map<String, String> requestBody) {
        try {
            String feedback = requestBody.get("feedback");
            ProjectApplicationDTO application = projectService.reviewApplication(applicationId, status, feedback);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "审核申请失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取项目的所有申请
    @GetMapping("/{projectId}/applications")
    public ResponseEntity<?> getProjectApplications(@PathVariable Long projectId) {
        try {
            List<ProjectApplicationDTO> applications = projectService.getProjectApplications(projectId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取项目申请失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 提交项目步骤
    @PostMapping("/{projectId}/steps/{stepNumber}/submit")
    public ResponseEntity<?> submitProjectStep(
            @PathVariable Long projectId,
            @PathVariable Integer stepNumber,
            @RequestParam Long studentId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String content = requestBody.get("content");
            String uploadedFile = requestBody.get("uploadedFile");
            String status = requestBody.get("status"); // 状态参数
            String batchNumberStr = requestBody.get("batchNumber"); // 批次号参数
            String briefDescription = requestBody.get("briefDescription"); // 内容简述
            String isFinalSubmissionStr = requestBody.get("isFinalSubmission"); // 是否为最终提交

            // 验证内容简述长度
            if (briefDescription != null && briefDescription.length() > 5) {
                briefDescription = briefDescription.substring(0, 5); // 截取前5个字符
            }

            Integer batchNumber = null;
            if (batchNumberStr != null && !batchNumberStr.isEmpty()) {
                try {
                    batchNumber = Integer.parseInt(batchNumberStr);
                } catch (NumberFormatException e) {
                    // 如果转换失败，忽略批次号
                }
            }

            // 解析是否为最终提交
            Boolean isFinalSubmission = Boolean.valueOf(isFinalSubmissionStr);

            // 如果提供了状态参数，直接设置状态
            if (status != null && !status.isEmpty()) {
                ProjectStepSubmissionDTO submission = projectService.submitProjectStepWithStatus(
                    projectId, studentId, stepNumber, content, uploadedFile, status, batchNumber, briefDescription, isFinalSubmission);
                return ResponseEntity.ok(submission);
            } else {
                // 如果指定了批次号，使用批次号
                if (batchNumber != null) {
                    ProjectStepSubmissionDTO submission = projectService.submitProjectStepWithStatus(
                        projectId, studentId, stepNumber, content, uploadedFile, "PENDING", batchNumber, briefDescription, isFinalSubmission);
                    return ResponseEntity.ok(submission);
                } else {
                    // 即使使用简化的方法，也需要传递最终提交标志
                    ProjectStepSubmissionDTO submission = projectService.submitProjectStepWithStatus(
                        projectId, studentId, stepNumber, content, uploadedFile, "PENDING", null, null, isFinalSubmission);
                    return ResponseEntity.ok(submission);
                }
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "提交项目步骤失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 审核项目步骤提交
    @PutMapping("/{projectId}/steps/{stepNumber}/review/{studentId}")
    public ResponseEntity<?> reviewStepSubmission(
            @PathVariable Long projectId,
            @PathVariable Integer stepNumber,
            @PathVariable Long studentId,
            @RequestParam String status,
            @RequestParam(required = false) Integer batchNumber,
            @RequestBody Map<String, String> requestBody) {
        try {
            String feedback = requestBody.get("feedback");
            ProjectStepSubmissionDTO submission = projectService.reviewStepSubmission(projectId, studentId, stepNumber, batchNumber, status, feedback);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "审核项目步骤失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取项目步骤提交（最新批次或指定批次）
    @GetMapping("/{projectId}/steps/{stepNumber}/submissions/{studentId}")
    public ResponseEntity<?> getProjectStepSubmission(
            @PathVariable Long projectId,
            @PathVariable Integer stepNumber,
            @PathVariable Long studentId,
            @RequestParam(required = false) Integer batchNumber) {
        try {
            ProjectStepSubmissionDTO submission = projectService.getProjectStepSubmission(projectId, studentId, stepNumber, batchNumber);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取项目步骤提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取项目的所有步骤提交
    @GetMapping("/{projectId}/steps/{stepNumber}/submissions")
    public ResponseEntity<?> getProjectStepSubmissions(
            @PathVariable Long projectId,
            @PathVariable Integer stepNumber) {
        try {
            List<ProjectStepSubmissionDTO> submissions = projectService.getProjectStepSubmissions(projectId, stepNumber);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取项目步骤提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取项目的所有提交（用于甘特图）
    @GetMapping("/{projectId}/submissions")
    public ResponseEntity<?> getAllProjectSubmissions(
            @PathVariable Long projectId) {
        try {
            List<ProjectStepSubmissionDTO> submissions = projectService.getAllProjectSubmissions(projectId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取项目所有提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取学生在特定项目特定步骤的所有批次提交
    @GetMapping("/{projectId}/steps/{stepNumber}/submissions/{studentId}/batches")
    public ResponseEntity<?> getStudentProjectStepBatchSubmissions(
            @PathVariable Long projectId,
            @PathVariable Integer stepNumber,
            @PathVariable Long studentId) {
        try {
            List<ProjectStepSubmissionDTO> submissions = projectService.getStudentProjectStepBatchSubmissions(projectId, studentId, stepNumber);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生批次提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 上传项目计划书
    @PutMapping("/{projectId}/plan")
    public ResponseEntity<?> uploadProjectPlan(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String projectPlan = requestBody.get("projectPlan");
            String uploadedFile = requestBody.get("uploadedFile");
            ProjectCollaborationDTO project = projectService.uploadProjectPlan(projectId, projectPlan, uploadedFile);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "上传项目计划书失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取项目的待审核申请
    @GetMapping("/{projectId}/applications/pending")
    public ResponseEntity<?> getPendingApplications(@PathVariable Long projectId) {
        try {
            List<ProjectApplicationDTO> applications = projectService.getPendingApplications(projectId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取待审核申请失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取学生的所有申请
    @GetMapping("/applications/student/{studentId}")
    public ResponseEntity<?> getStudentApplications(@PathVariable Long studentId) {
        try {
            List<ProjectApplicationDTO> applications = projectService.getStudentApplications(studentId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生申请失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取申请详情
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<?> getApplicationById(@PathVariable Long applicationId) {
        try {
            ProjectApplicationDTO application = projectService.getApplicationById(applicationId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取申请详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取学生参与的项目
    @GetMapping("/student/{studentId}/participated")
    public ResponseEntity<?> getStudentParticipatedProjects(@PathVariable Long studentId) {
        try {
            List<ProjectCollaborationDTO> projects = projectService.getStudentParticipatedProjects(studentId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生参与项目失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 直接审核项目步骤提交（通过提交ID）
    @PutMapping("/steps/submissions/{submissionId}/review")
    public ResponseEntity<?> reviewStepSubmissionById(
            @PathVariable Long submissionId,
            @RequestParam String status,
            @RequestBody Map<String, String> requestBody) {
        try {
            String feedback = requestBody.get("feedback");
            ProjectStepSubmissionDTO submission = projectService.reviewProjectStepSubmission(submissionId, status, feedback);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "审核项目步骤提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取学生的所有步骤提交
    @GetMapping("/steps/student/{studentId}/submissions")
    public ResponseEntity<?> getStudentStepSubmissions(@PathVariable Long studentId) {
        try {
            List<ProjectStepSubmissionDTO> submissions = projectService.getStudentStepSubmissions(studentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生步骤提交列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取学生在特定项目的所有步骤提交
    @GetMapping("/{projectId}/student/{studentId}/steps/submissions")
    public ResponseEntity<?> getStudentProjectStepSubmissions(
            @PathVariable Long projectId,
            @PathVariable Long studentId) {
        try {
            List<ProjectStepSubmissionDTO> submissions = projectService.getStudentProjectStepSubmissions(projectId, studentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生项目步骤提交列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 提交实训报告
    @PostMapping("/{projectId}/training-report")
    public ResponseEntity<?> submitTrainingReport(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String title = requestBody.get("title");
            String content = requestBody.get("content");
            String uploadedFile = requestBody.get("uploadedFile");
            Long studentId = Long.parseLong(requestBody.get("studentId"));

            TrainingReportDTO report = projectService.submitTrainingReport(projectId, studentId, title, content, uploadedFile);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "提交实训报告失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取项目的所有实训报告
    @GetMapping("/{projectId}/training-reports")
    public ResponseEntity<?> getProjectTrainingReports(@PathVariable Long projectId) {
        try {
            List<TrainingReportDTO> reports = projectService.getProjectTrainingReports(projectId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取项目实训报告失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取学生的实训报告
    @GetMapping("/{projectId}/training-report/{studentId}")
    public ResponseEntity<?> getStudentTrainingReport(
            @PathVariable Long projectId,
            @PathVariable Long studentId) {
        try {
            TrainingReportDTO report = projectService.getStudentTrainingReport(projectId, studentId);
            if (report == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "学生尚未提交实训报告");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取学生实训报告失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 提交订单培养步骤
    @PostMapping("/{projectId}/order-training/step/{stepNumber}")
    public ResponseEntity<?> submitOrderTrainingStep(
            @PathVariable Long projectId,
            @PathVariable Integer stepNumber,
            @RequestBody Map<String, String> requestBody) {
        try {
            String title = requestBody.get("title");
            String content = requestBody.get("content");
            String fileUrl = requestBody.get("fileUrl");
            Long userId = Long.parseLong(requestBody.get("userId"));
            String userRole = requestBody.get("userRole");

            // 如果没有提供用户角色，根据用户ID判断
            if (userRole == null || userRole.isEmpty()) {
                // 假设用户ID小于10000的是企业，大于等于10000的是学生
                // 这只是一个示例，实际应用中应该有更准确的判断方式
                userRole = userId < 10000 ? "企业" : "学生";
            }

            OrderTrainingStepDTO step = projectService.submitOrderTrainingStep(projectId, userId, userRole, stepNumber, title, content, fileUrl);
            return ResponseEntity.ok(step);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "提交订单培养步骤失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取订单培养步骤
    @GetMapping("/{projectId}/order-training/step/{stepNumber}")
    public ResponseEntity<?> getOrderTrainingStep(
            @PathVariable Long projectId,
            @PathVariable Integer stepNumber) {
        try {
            OrderTrainingStepDTO step = projectService.getOrderTrainingStep(projectId, stepNumber);
            return ResponseEntity.ok(step);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取订单培养步骤失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
