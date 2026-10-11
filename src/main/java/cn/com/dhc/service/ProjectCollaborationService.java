package cn.com.dhc.service;

import cn.com.dhc.Entity.*;
import cn.com.dhc.dto.ProjectApplicationDTO;
import cn.com.dhc.dto.ProjectCollaborationDTO;
import cn.com.dhc.dto.ProjectStepSubmissionDTO;
import cn.com.dhc.dto.TrainingReportDTO;
import cn.com.dhc.dto.OrderTrainingStepDTO;
import cn.com.dhc.mapper.*;
import cn.com.dhc.repository.TrainingReportRepository;
import cn.com.dhc.repository.OrderTrainingStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectCollaborationService {

    @Autowired
    private ProjectCollaborationRepository projectRepository;

    @Autowired
    private ProjectApplicationRepository applicationRepository;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProjectStepSubmissionRepository stepSubmissionRepository;

    @Autowired
    private TrainingReportRepository trainingReportRepository;

    @Autowired
    private OrderTrainingStepRepository orderTrainingStepRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 创建新项目
    @Transactional
    public ProjectCollaborationDTO createProject(ProjectCollaborationDTO projectDTO) {
        Optional<Enterprise> enterpriseOpt = enterpriseRepository.findById(projectDTO.getEnterpriseId());
        if (!enterpriseOpt.isPresent()) {
            throw new RuntimeException("企业不存在");
        }

        ProjectCollaboration project = new ProjectCollaboration();
        project.setEnterprise(enterpriseOpt.get());
        project.setTitle(projectDTO.getTitle());
        project.setDescription(projectDTO.getDescription());
        project.setProjectDirection(projectDTO.getProjectDirection());
        project.setRequiredSkills(projectDTO.getRequiredSkills());
        project.setMaxParticipants(projectDTO.getMaxParticipants());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setStatus("OPEN");
        project.setCreateTime(LocalDateTime.now());
        project.setCollaborationType(projectDTO.getCollaborationType());
        project.setLocation(projectDTO.getLocation());
        project.setTrainingPlan(projectDTO.getTrainingPlan());
        project.setTimeCycle(projectDTO.getTimeCycle());
        project.setResources(projectDTO.getResources());
        project.setSupport(projectDTO.getSupport());
        project.setTechRequirement(projectDTO.getTechRequirement());
        project.setProjectPlan(projectDTO.getProjectPlan());
        project.setCurrentStep(1); // 初始步骤为1

        ProjectCollaboration savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    // 更新项目信息
    @Transactional
    public ProjectCollaborationDTO updateProject(Long projectId, ProjectCollaborationDTO projectDTO) {
        Optional<ProjectCollaboration> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new RuntimeException("项目不存在");
        }

        ProjectCollaboration project = projectOpt.get();
        project.setTitle(projectDTO.getTitle());
        project.setDescription(projectDTO.getDescription());
        project.setProjectDirection(projectDTO.getProjectDirection());
        project.setRequiredSkills(projectDTO.getRequiredSkills());
        project.setMaxParticipants(projectDTO.getMaxParticipants());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setCollaborationType(projectDTO.getCollaborationType());
        project.setLocation(projectDTO.getLocation());
        project.setTrainingPlan(projectDTO.getTrainingPlan());
        project.setTimeCycle(projectDTO.getTimeCycle());
        project.setResources(projectDTO.getResources());
        project.setSupport(projectDTO.getSupport());
        project.setTechRequirement(projectDTO.getTechRequirement());
        project.setProjectPlan(projectDTO.getProjectPlan());
        // 不更新状态、创建时间和企业信息

        ProjectCollaboration updatedProject = projectRepository.save(project);
        return convertToDTO(updatedProject);
    }

    // 更改项目状态
    @Transactional
    public ProjectCollaborationDTO updateProjectStatus(Long projectId, String status) {
        Optional<ProjectCollaboration> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new RuntimeException("项目不存在");
        }

        ProjectCollaboration project = projectOpt.get();
        project.setStatus(status);

        ProjectCollaboration updatedProject = projectRepository.save(project);
        return convertToDTO(updatedProject);
    }

    // 获取项目详情
    public ProjectCollaborationDTO getProject(Long projectId) {
        Optional<ProjectCollaboration> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new RuntimeException("项目不存在");
        }

        return convertToDTO(projectOpt.get());
    }

    // 获取企业的所有项目
    public List<ProjectCollaborationDTO> getEnterpriseProjects(Long enterpriseId) {
        List<ProjectCollaboration> projects = projectRepository.findByEnterprise_EnterpriseId(enterpriseId);
        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 删除项目
    @Transactional
    public void deleteProject(Long enterpriseId, Long projectId) {
        Optional<ProjectCollaboration> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new RuntimeException("项目不存在");
        }

        ProjectCollaboration project = projectOpt.get();

        // 验证项目属于该企业
        if (!project.getEnterprise().getEnterpriseId().equals(enterpriseId)) {
            throw new RuntimeException("无权操作该项目");
        }

        // 删除项目相关的步骤提交记录
        List<ProjectStepSubmission> stepSubmissions = stepSubmissionRepository.findByProject_ProjectId(projectId);
        if (!stepSubmissions.isEmpty()) {
            stepSubmissionRepository.deleteAll(stepSubmissions);
        }

        // 删除项目相关的申请记录
        List<ProjectApplication> applications = applicationRepository.findByProject_ProjectId(projectId);
        if (!applications.isEmpty()) {
            applicationRepository.deleteAll(applications);
        }

        // 删除项目
        projectRepository.delete(project);
    }

    // 获取企业的开放项目
    public List<ProjectCollaborationDTO> getEnterpriseOpenProjects(Long enterpriseId) {
        List<ProjectCollaboration> projects = projectRepository.findByEnterprise_EnterpriseIdAndStatus(enterpriseId, "OPEN");
        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 分页获取所有开放项目
    public Page<ProjectCollaborationDTO> getAllOpenProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ProjectCollaboration> projectPage = projectRepository.findByStatus("OPEN", pageable);

        return projectPage.map(this::convertToDTO);
    }

    // 根据项目方向筛选项目
    public List<ProjectCollaborationDTO> filterProjectsByDirection(String direction) {
        List<ProjectCollaboration> projects = projectRepository.findByProjectDirectionAndStatus(direction, "OPEN");
        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 搜索项目
    public Page<ProjectCollaborationDTO> searchProjects(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ProjectCollaboration> projectPage = projectRepository.searchProjects(keyword, "OPEN", pageable);

        return projectPage.map(this::convertToDTO);
    }

    // 学生申请参与项目
    @Transactional
    public ProjectApplicationDTO applyForProject(Long projectId, Long studentId, String applicationReason,
                                               String uploadedFile, String notes, String relevantExperience, String contactInfo) {
        Optional<ProjectCollaboration> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new RuntimeException("项目不存在");
        }

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            throw new RuntimeException("学生不存在");
        }

        // 检查是否已经申请过
        Optional<ProjectApplication> existingApplication = applicationRepository.findByProject_ProjectIdAndStudent_StudentId(projectId, studentId);
        if (existingApplication.isPresent()) {
            // 如果申请状态是被拒绝，允许重新申请
            if ("REJECTED".equals(existingApplication.get().getStatus())) {
                // 更新现有申请
                ProjectApplication application = existingApplication.get();
                application.setApplicationReason(applicationReason);
                application.setStatus("PENDING");
                application.setApplyTime(LocalDateTime.now());
                application.setUploadedFile(uploadedFile);
                application.setNotes(notes);
                application.setRelevantExperience(relevantExperience);
                application.setContactInfo(contactInfo);
                application.setReviewTime(null);
                application.setFeedback(null);

                ProjectApplication savedApplication = applicationRepository.save(application);
                return convertToApplicationDTO(savedApplication);
            } else if ("PENDING".equals(existingApplication.get().getStatus()) || "APPROVED".equals(existingApplication.get().getStatus())) {
                throw new RuntimeException("您已经申请过该项目");
            }
        }

        // 检查项目是否已满
        long approvedCount = applicationRepository.countByProject_ProjectIdAndStatus(projectId, "APPROVED");
        if (approvedCount >= projectOpt.get().getMaxParticipants()) {
            throw new RuntimeException("项目参与人数已满");
        }

        // 创建申请
        ProjectApplication application = new ProjectApplication();
        application.setProject(projectOpt.get());
        application.setStudent(studentOpt.get());
        application.setApplicationReason(applicationReason);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        application.setUploadedFile(uploadedFile);
        application.setNotes(notes);
        application.setRelevantExperience(relevantExperience);
        application.setContactInfo(contactInfo);

        ProjectApplication savedApplication = applicationRepository.save(application);
        return convertToApplicationDTO(savedApplication);
    }

    // 企业审核学生申请
    @Transactional
    public ProjectApplicationDTO reviewApplication(Long applicationId, String status, String feedback) {
        Optional<ProjectApplication> applicationOpt = applicationRepository.findById(applicationId);
        if (!applicationOpt.isPresent()) {
            throw new RuntimeException("申请不存在");
        }

        ProjectApplication application = applicationOpt.get();

        // 如果要通过申请，检查项目是否已满
        if ("APPROVED".equals(status)) {
            long approvedCount = applicationRepository.countByProject_ProjectIdAndStatus(application.getProject().getProjectId(), "APPROVED");
            if (approvedCount >= application.getProject().getMaxParticipants()) {
                throw new RuntimeException("项目参与人数已满");
            }
        }

        application.setStatus(status);
        application.setFeedback(feedback);
        application.setReviewTime(LocalDateTime.now());

        ProjectApplication updatedApplication = applicationRepository.save(application);
        return convertToApplicationDTO(updatedApplication);
    }

    // 获取项目的所有申请
    public List<ProjectApplicationDTO> getProjectApplications(Long projectId) {
        List<ProjectApplication> applications = applicationRepository.findByProject_ProjectId(projectId);
        return applications.stream()
                .map(this::convertToApplicationDTO)
                .collect(Collectors.toList());
    }

    // 获取项目的待审核申请
    public List<ProjectApplicationDTO> getPendingApplications(Long projectId) {
        List<ProjectApplication> applications = applicationRepository.findByProject_ProjectIdAndStatus(projectId, "PENDING");
        return applications.stream()
                .map(this::convertToApplicationDTO)
                .collect(Collectors.toList());
    }

    // 获取学生的所有申请
    public List<ProjectApplicationDTO> getStudentApplications(Long studentId) {
        List<ProjectApplication> applications = applicationRepository.findByStudent_StudentId(studentId);
        return applications.stream()
                .map(this::convertToApplicationDTO)
                .collect(Collectors.toList());
    }

    // 获取申请详情
    public ProjectApplicationDTO getApplicationById(Long applicationId) {
        Optional<ProjectApplication> applicationOpt = applicationRepository.findById(applicationId);
        if (!applicationOpt.isPresent()) {
            throw new RuntimeException("申请不存在");
        }
        return convertToApplicationDTO(applicationOpt.get());
    }

    // 获取学生参与的项目
    public List<ProjectCollaborationDTO> getStudentParticipatedProjects(Long studentId) {
        List<Long> projectIds = applicationRepository.findApprovedProjectIdsByStudentId(studentId);
        return projectIds.stream()
                .map(projectId -> {
                    try {
                        return getProject(projectId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(project -> project != null)
                .collect(Collectors.toList());
    }

    // 将实体转换为DTO
    private ProjectCollaborationDTO convertToDTO(ProjectCollaboration project) {
        ProjectCollaborationDTO dto = new ProjectCollaborationDTO();
        dto.setProjectId(project.getProjectId());
        dto.setEnterpriseId(project.getEnterprise().getEnterpriseId());
        dto.setEnterpriseName(project.getEnterprise().getName());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setProjectDirection(project.getProjectDirection());
        dto.setRequiredSkills(project.getRequiredSkills());
        dto.setMaxParticipants(project.getMaxParticipants());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setStatus(project.getStatus());
        dto.setCreateTime(project.getCreateTime());

        // 格式化创建时间
        if (project.getCreateTime() != null) {
            dto.setFormattedCreateTime(project.getCreateTime().format(formatter));
        }

        // 获取申请数量和已通过数量
        dto.setApplicationCount((int) applicationRepository.countByProject_ProjectId(project.getProjectId()));
        dto.setApprovedCount((int) applicationRepository.countByProject_ProjectIdAndStatus(project.getProjectId(), "APPROVED"));

        // 设置新增字段
        dto.setCollaborationType(project.getCollaborationType());
        dto.setLocation(project.getLocation());
        dto.setTrainingPlan(project.getTrainingPlan());
        dto.setTimeCycle(project.getTimeCycle());
        dto.setResources(project.getResources());
        dto.setSupport(project.getSupport());
        dto.setTechRequirement(project.getTechRequirement());
        dto.setProjectPlan(project.getProjectPlan());
        dto.setProjectPlanFile(project.getProjectPlanFile());
        dto.setCurrentStep(project.getCurrentStep());

        return dto;
    }

    // 将申请实体转换为DTO
    private ProjectApplicationDTO convertToApplicationDTO(ProjectApplication application) {
        ProjectApplicationDTO dto = new ProjectApplicationDTO();
        dto.setApplicationId(application.getApplicationId());
        dto.setProjectId(application.getProject().getProjectId());
        dto.setProjectTitle(application.getProject().getTitle());
        dto.setStudentId(application.getStudent().getStudentId());
        dto.setStudentName(application.getStudent().getName());
        dto.setStudentSchool(application.getStudent().getSchool());
        dto.setApplicationReason(application.getApplicationReason());
        dto.setStatus(application.getStatus());
        dto.setApplyTime(application.getApplyTime());
        dto.setReviewTime(application.getReviewTime());
        dto.setFeedback(application.getFeedback());

        // 格式化时间
        if (application.getApplyTime() != null) {
            dto.setFormattedApplyTime(application.getApplyTime().format(formatter));
        }

        if (application.getReviewTime() != null) {
            dto.setFormattedReviewTime(application.getReviewTime().format(formatter));
        }

        // 设置新增字段
        dto.setUploadedFile(application.getUploadedFile());
        dto.setNotes(application.getNotes());
        dto.setRelevantExperience(application.getRelevantExperience());
        dto.setContactInfo(application.getContactInfo());

        return dto;
    }

    // 将步骤提交实体转换为DTO
    private ProjectStepSubmissionDTO convertToStepSubmissionDTO(ProjectStepSubmission submission) {
        ProjectStepSubmissionDTO dto = new ProjectStepSubmissionDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setProjectId(submission.getProject().getProjectId());
        dto.setProjectTitle(submission.getProject().getTitle());
        dto.setStudentId(submission.getStudent().getStudentId());
        dto.setStudentName(submission.getStudent().getName());
        dto.setStudentSchool(submission.getStudent().getSchool());
        dto.setStepNumber(submission.getStepNumber());
        dto.setContent(submission.getContent());
        dto.setUploadedFile(submission.getUploadedFile());
        dto.setStatus(submission.getStatus());
        dto.setSubmissionTime(submission.getSubmissionTime());
        dto.setReviewTime(submission.getReviewTime());
        dto.setFeedback(submission.getFeedback());
        dto.setBatchNumber(submission.getBatchNumber());
        dto.setBriefDescription(submission.getBriefDescription());
        dto.setIsFinalSubmission(submission.getIsFinalSubmission());

        // 格式化时间
        if (submission.getSubmissionTime() != null) {
            dto.setFormattedSubmissionTime(submission.getSubmissionTime().format(formatter));
        }

        if (submission.getReviewTime() != null) {
            dto.setFormattedReviewTime(submission.getReviewTime().format(formatter));
        }

        return dto;
    }

    // 提交项目步骤
    @Transactional
    public ProjectStepSubmissionDTO submitProjectStep(Long projectId, Long studentId, Integer stepNumber, String content, String uploadedFile) {
        return submitProjectStepWithStatus(projectId, studentId, stepNumber, content, uploadedFile, "PENDING", null, null);
    }

    // 提交项目步骤并设置状态
    @Transactional
    public ProjectStepSubmissionDTO submitProjectStepWithStatus(Long projectId, Long studentId, Integer stepNumber, String content, String uploadedFile, String status) {
        return submitProjectStepWithStatus(projectId, studentId, stepNumber, content, uploadedFile, status, null, null);
    }

    // 提交项目步骤并设置状态和批次号
    @Transactional
    public ProjectStepSubmissionDTO submitProjectStepWithStatus(Long projectId, Long studentId, Integer stepNumber, String content, String uploadedFile, String status, Integer batchNumber) {
        return submitProjectStepWithStatus(projectId, studentId, stepNumber, content, uploadedFile, status, batchNumber, null);
    }

    // 提交项目步骤并设置状态、批次号和内容简述
    @Transactional
    public ProjectStepSubmissionDTO submitProjectStepWithStatus(Long projectId, Long studentId, Integer stepNumber, String content, String uploadedFile, String status, Integer batchNumber, String briefDescription) {
        return submitProjectStepWithStatus(projectId, studentId, stepNumber, content, uploadedFile, status, batchNumber, briefDescription, false);
    }

    // 提交项目步骤并设置状态、批次号、内容简述和是否为最终提交
    @Transactional
    public ProjectStepSubmissionDTO submitProjectStepWithStatus(Long projectId, Long studentId, Integer stepNumber, String content, String uploadedFile, String status, Integer batchNumber, String briefDescription, Boolean isFinalSubmission) {
        Optional<ProjectCollaboration> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new RuntimeException("项目不存在");
        }

        ProjectCollaboration project = projectOpt.get();

        // 检查是否为产学研合作项目
        if (!"产学研合作".equals(project.getCollaborationType())) {
            throw new RuntimeException("只有产学研合作项目才能提交步骤");
        }

        // 检查当前项目步骤
        if (project.getCurrentStep() < stepNumber) {
            throw new RuntimeException("当前项目还未进入第" + stepNumber + "步");
        }

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            throw new RuntimeException("学生不存在");
        }

        // 检查学生是否已经申请并被批准参与项目
        Optional<ProjectApplication> applicationOpt = applicationRepository.findByProject_ProjectIdAndStudent_StudentId(projectId, studentId);
        if (!applicationOpt.isPresent() || !"APPROVED".equals(applicationOpt.get().getStatus())) {
            throw new RuntimeException("您尚未被批准参与该项目");
        }

        // 如果是第3步，允许分批次提交
        ProjectStepSubmission submission;
        if (stepNumber == 3) {
            // 如果没有指定批次号，自动获取下一个批次号
            if (batchNumber == null) {
                Integer maxBatchNumber = stepSubmissionRepository.findMaxBatchNumberByProjectIdAndStudentIdAndStepNumber(projectId, studentId, stepNumber);
                batchNumber = (maxBatchNumber == null) ? 1 : maxBatchNumber + 1;
            }

            // 检查指定批次是否已存在
            Optional<ProjectStepSubmission> existingBatchSubmissionOpt = stepSubmissionRepository.findByProject_ProjectIdAndStudent_StudentIdAndStepNumberAndBatchNumber(
                projectId, studentId, stepNumber, batchNumber);

            if (existingBatchSubmissionOpt.isPresent()) {
                // 如果批次已存在，更新内容
                submission = existingBatchSubmissionOpt.get();
                if ("APPROVED".equals(submission.getStatus()) && !"APPROVED".equals(status)) {
                    throw new RuntimeException("该批次已经审核通过，无法修改");
                }

                // 更新提交内容
                submission.setContent(content);
                submission.setUploadedFile(uploadedFile);
                submission.setStatus(status);
                submission.setSubmissionTime(LocalDateTime.now());
                submission.setBriefDescription(briefDescription);
                submission.setIsFinalSubmission(isFinalSubmission);

                // 如果是审核状态，设置审核时间
                if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
                    submission.setReviewTime(LocalDateTime.now());
                } else {
                    submission.setReviewTime(null);
                    submission.setFeedback(null);
                }
            } else {
                // 创建新的批次提交
                submission = new ProjectStepSubmission();
                submission.setProject(project);
                submission.setStudent(studentOpt.get());
                submission.setStepNumber(stepNumber);
                submission.setContent(content);
                submission.setUploadedFile(uploadedFile);
                submission.setStatus(status);
                submission.setSubmissionTime(LocalDateTime.now());
                submission.setBatchNumber(batchNumber);
                submission.setBriefDescription(briefDescription);
                submission.setIsFinalSubmission(isFinalSubmission);

                // 如果是审核状态，设置审核时间
                if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
                    submission.setReviewTime(LocalDateTime.now());
                }
            }
        } else {
            // 其他步骤不允许分批次，使用原来的逻辑
            Optional<ProjectStepSubmission> existingSubmissionOpt = stepSubmissionRepository.findFirstByProject_ProjectIdAndStudent_StudentIdAndStepNumberOrderByBatchNumberDesc(
                projectId, studentId, stepNumber);

            if (existingSubmissionOpt.isPresent()) {
                // 如果已经提交过，检查状态
                submission = existingSubmissionOpt.get();
                if ("APPROVED".equals(submission.getStatus()) && !"APPROVED".equals(status)) {
                    throw new RuntimeException("您已经完成了该步骤");
                }

                // 更新提交内容
                submission.setContent(content);
                submission.setUploadedFile(uploadedFile);
                submission.setStatus(status);
                submission.setSubmissionTime(LocalDateTime.now());
                submission.setIsFinalSubmission(isFinalSubmission);

                // 如果是审核状态，设置审核时间
                if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
                    submission.setReviewTime(LocalDateTime.now());
                } else {
                    submission.setReviewTime(null);
                    submission.setFeedback(null);
                }
            } else {
                // 创建新的提交
                submission = new ProjectStepSubmission();
                submission.setProject(project);
                submission.setStudent(studentOpt.get());
                submission.setStepNumber(stepNumber);
                submission.setContent(content);
                submission.setUploadedFile(uploadedFile);
                submission.setStatus(status);
                submission.setSubmissionTime(LocalDateTime.now());
                submission.setBatchNumber(1); // 非第3步的批次号固定为1
                submission.setIsFinalSubmission(isFinalSubmission);

                // 如果是审核状态，设置审核时间
                if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
                    submission.setReviewTime(LocalDateTime.now());
                }
            }
        }

        ProjectStepSubmission savedSubmission = stepSubmissionRepository.save(submission);

        // 如果是审核通过，检查是否需要更新项目当前步骤
        if ("APPROVED".equals(status)) {
            // 如果是最后一步，将项目状态设置为已完成
            if (stepNumber == 4) {
                project.setStatus("COMPLETED");
            }
            // 如果当前步骤的所有学生都已经通过，进入下一步
            else {
                int currentStep = project.getCurrentStep();
                long approvedCount = stepSubmissionRepository.countByProject_ProjectIdAndStepNumberAndStatus(project.getProjectId(), currentStep, "APPROVED");
                long totalParticipants = applicationRepository.countByProject_ProjectIdAndStatus(project.getProjectId(), "APPROVED");

                if (approvedCount == totalParticipants) {
                    project.setCurrentStep(currentStep + 1);
                }
            }

            projectRepository.save(project);
        }

        return convertToStepSubmissionDTO(savedSubmission);
    }

    // 审核项目步骤提交
    @Transactional
    public ProjectStepSubmissionDTO reviewProjectStepSubmission(Long submissionId, String status, String feedback) {
        Optional<ProjectStepSubmission> submissionOpt = stepSubmissionRepository.findById(submissionId);
        if (!submissionOpt.isPresent()) {
            throw new RuntimeException("提交不存在");
        }

        ProjectStepSubmission submission = submissionOpt.get();
        submission.setStatus(status);
        submission.setFeedback(feedback);
        submission.setReviewTime(LocalDateTime.now());

        // 如果审核通过，检查是否需要更新项目当前步骤
        if ("APPROVED".equals(status)) {
            ProjectCollaboration project = submission.getProject();

            // 如果是最后一步，将项目状态设置为已完成
            if (submission.getStepNumber() == 4) {
                project.setStatus("COMPLETED");
            }
            // 如果是最终提交并且已经审核通过，则进入第4步
            else if (submission.getStepNumber() == 3 && Boolean.TRUE.equals(submission.getIsFinalSubmission())) {
                project.setCurrentStep(4);
            }
            // 如果当前步骤的所有学生都已经通过，进入下一步
            else {
                int currentStep = project.getCurrentStep();
                long approvedCount = stepSubmissionRepository.countByProject_ProjectIdAndStepNumberAndStatus(project.getProjectId(), currentStep, "APPROVED");
                long totalParticipants = applicationRepository.countByProject_ProjectIdAndStatus(project.getProjectId(), "APPROVED");

                if (approvedCount == totalParticipants) {
                    project.setCurrentStep(currentStep + 1);
                }
            }

            projectRepository.save(submission.getProject());
        }

        ProjectStepSubmission updatedSubmission = stepSubmissionRepository.save(submission);
        return convertToStepSubmissionDTO(updatedSubmission);
    }

    // 上传项目计划书
    @Transactional
    public ProjectCollaborationDTO uploadProjectPlan(Long projectId, String projectPlan, String uploadedFile) {
        Optional<ProjectCollaboration> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new RuntimeException("项目不存在");
        }

        ProjectCollaboration project = projectOpt.get();

        // 检查是否为产学研合作项目
        if (!"产学研合作".equals(project.getCollaborationType())) {
            throw new RuntimeException("只有产学研合作项目才能上传计划书");
        }

        // 检查当前项目步骤
        if (project.getCurrentStep() != 2) {
            throw new RuntimeException("当前项目不在第2步，无法上传计划书");
        }

        project.setProjectPlan(projectPlan);
        project.setProjectPlanFile(uploadedFile);
        ProjectCollaboration updatedProject = projectRepository.save(project);
        return convertToDTO(updatedProject);
    }

    // 获取项目步骤提交（最新批次）
    public ProjectStepSubmissionDTO getProjectStepSubmission(Long projectId, Long studentId, Integer stepNumber) {
        return getProjectStepSubmission(projectId, studentId, stepNumber, null);
    }

    // 获取项目步骤的指定批次提交
    public ProjectStepSubmissionDTO getProjectStepSubmission(Long projectId, Long studentId, Integer stepNumber, Integer batchNumber) {
        Optional<ProjectStepSubmission> submissionOpt;

        if (batchNumber != null) {
            // 如果指定了批次号，获取指定批次
            submissionOpt = stepSubmissionRepository.findByProject_ProjectIdAndStudent_StudentIdAndStepNumberAndBatchNumber(
                projectId, studentId, stepNumber, batchNumber);
        } else {
            // 如果没有指定批次号，获取最新批次
            submissionOpt = stepSubmissionRepository.findFirstByProject_ProjectIdAndStudent_StudentIdAndStepNumberOrderByBatchNumberDesc(
                projectId, studentId, stepNumber);
        }

        if (!submissionOpt.isPresent()) {
            throw new RuntimeException("提交不存在");
        }

        return convertToStepSubmissionDTO(submissionOpt.get());
    }

    // 获取项目的所有步骤提交
    public List<ProjectStepSubmissionDTO> getProjectStepSubmissions(Long projectId, Integer stepNumber) {
        List<ProjectStepSubmission> submissions = stepSubmissionRepository.findByProject_ProjectIdAndStepNumber(projectId, stepNumber);
        return submissions.stream()
                .map(this::convertToStepSubmissionDTO)
                .collect(Collectors.toList());
    }

    // 获取项目的所有提交（用于甘特图）
    public List<ProjectStepSubmissionDTO> getAllProjectSubmissions(Long projectId) {
        List<ProjectStepSubmission> submissions = stepSubmissionRepository.findByProject_ProjectId(projectId);
        return submissions.stream()
                .map(this::convertToStepSubmissionDTO)
                .collect(Collectors.toList());
    }

    // 获取学生的所有步骤提交
    public List<ProjectStepSubmissionDTO> getStudentStepSubmissions(Long studentId) {
        List<ProjectStepSubmission> submissions = stepSubmissionRepository.findByStudent_StudentId(studentId);
        return submissions.stream()
                .map(this::convertToStepSubmissionDTO)
                .collect(Collectors.toList());
    }

    // 获取学生在特定项目的所有步骤提交
    public List<ProjectStepSubmissionDTO> getStudentProjectStepSubmissions(Long projectId, Long studentId) {
        List<ProjectStepSubmission> submissions = stepSubmissionRepository.findByProject_ProjectIdAndStudent_StudentId(projectId, studentId);
        return submissions.stream()
                .map(this::convertToStepSubmissionDTO)
                .collect(Collectors.toList());
    }

    // 获取学生在特定项目特定步骤的所有批次提交
    public List<ProjectStepSubmissionDTO> getStudentProjectStepBatchSubmissions(Long projectId, Long studentId, Integer stepNumber) {
        List<ProjectStepSubmission> submissions = stepSubmissionRepository.findByProject_ProjectIdAndStudent_StudentIdAndStepNumberOrderByBatchNumberDesc(projectId, studentId, stepNumber);
        return submissions.stream()
                .map(this::convertToStepSubmissionDTO)
                .collect(Collectors.toList());
    }

    // 审核项目步骤提交 - 通过项目、学生和步骤号
    @Transactional
    public ProjectStepSubmissionDTO reviewStepSubmission(Long projectId, Long studentId, Integer stepNumber, String status, String feedback) {
        return reviewStepSubmission(projectId, studentId, stepNumber, null, status, feedback);
    }

    // 审核项目步骤提交 - 通过项目、学生、步骤号和批次号
    @Transactional
    public ProjectStepSubmissionDTO reviewStepSubmission(Long projectId, Long studentId, Integer stepNumber, Integer batchNumber, String status, String feedback) {
        // 获取提交记录
        Optional<ProjectStepSubmission> submissionOpt;

        if (batchNumber != null) {
            // 如果指定了批次号，获取指定批次
            submissionOpt = stepSubmissionRepository.findByProject_ProjectIdAndStudent_StudentIdAndStepNumberAndBatchNumber(
                projectId, studentId, stepNumber, batchNumber);
        } else {
            // 如果没有指定批次号，获取最新批次
            submissionOpt = stepSubmissionRepository.findFirstByProject_ProjectIdAndStudent_StudentIdAndStepNumberOrderByBatchNumberDesc(
                projectId, studentId, stepNumber);
        }

        if (!submissionOpt.isPresent()) {
            throw new RuntimeException("提交不存在");
        }

        // 调用现有的reviewProjectStepSubmission方法
        return reviewProjectStepSubmission(submissionOpt.get().getSubmissionId(), status, feedback);
    }

    // 提交实训报告
    @Transactional
    public TrainingReportDTO submitTrainingReport(Long projectId, Long studentId, String title, String content, String uploadedFile) {
        // 检查项目是否存在
        ProjectCollaboration project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        // 检查学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        // 检查是否已经提交过报告
        Optional<TrainingReport> existingReport = trainingReportRepository.findByProjectIdAndStudentId(projectId, studentId);

        TrainingReport report;
        if (existingReport.isPresent()) {
            // 更新现有报告
            report = existingReport.get();
            report.setTitle(title);
            report.setContent(content);
            report.setUploadedFile(uploadedFile);
            report.setSubmissionTime(LocalDateTime.now());
        } else {
            // 创建新报告
            report = new TrainingReport();
            report.setProjectId(projectId);
            report.setStudentId(studentId);
            report.setTitle(title);
            report.setContent(content);
            report.setUploadedFile(uploadedFile);
            report.setSubmissionTime(LocalDateTime.now());
        }

        report = trainingReportRepository.save(report);

        // 转换为DTO
        TrainingReportDTO dto = new TrainingReportDTO();
        dto.setId(report.getId());
        dto.setProjectId(report.getProjectId());
        dto.setStudentId(report.getStudentId());
        dto.setTitle(report.getTitle());
        dto.setContent(report.getContent());
        dto.setUploadedFile(report.getUploadedFile());
        dto.setSubmissionTime(report.getSubmissionTime());
        dto.setStudentName(student.getName());
        dto.setStudentSchool(student.getSchool());

        return dto;
    }

    // 获取项目的所有实训报告
    public List<TrainingReportDTO> getProjectTrainingReports(Long projectId) {
        List<TrainingReport> reports = trainingReportRepository.findByProjectId(projectId);

        return reports.stream().map(report -> {
            TrainingReportDTO dto = new TrainingReportDTO();
            dto.setId(report.getId());
            dto.setProjectId(report.getProjectId());
            dto.setStudentId(report.getStudentId());
            dto.setTitle(report.getTitle());
            dto.setContent(report.getContent());
            dto.setUploadedFile(report.getUploadedFile());
            dto.setSubmissionTime(report.getSubmissionTime());

            // 获取学生信息
            studentRepository.findById(report.getStudentId()).ifPresent(student -> {
                dto.setStudentName(student.getName());
                dto.setStudentSchool(student.getSchool());
            });

            return dto;
        }).collect(Collectors.toList());
    }

    // 获取学生的实训报告
    public TrainingReportDTO getStudentTrainingReport(Long projectId, Long studentId) {
        Optional<TrainingReport> reportOpt = trainingReportRepository.findByProjectIdAndStudentId(projectId, studentId);

        return reportOpt.map(report -> {
            TrainingReportDTO dto = new TrainingReportDTO();
            dto.setId(report.getId());
            dto.setProjectId(report.getProjectId());
            dto.setStudentId(report.getStudentId());
            dto.setTitle(report.getTitle());
            dto.setContent(report.getContent());
            dto.setUploadedFile(report.getUploadedFile());
            dto.setSubmissionTime(report.getSubmissionTime());

            // 获取学生信息
            studentRepository.findById(report.getStudentId()).ifPresent(student -> {
                dto.setStudentName(student.getName());
                dto.setStudentSchool(student.getSchool());
            });

            return dto;
        }).orElse(null);
    }

    // 提交订单培养步骤
    @Transactional
    public OrderTrainingStepDTO submitOrderTrainingStep(Long projectId, Long userId, String userRole, Integer stepNumber, String title, String content, String fileUrl) {
        // 检查项目是否存在
        ProjectCollaboration project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        // 检查是否为订单培养项目
        if (!"订单培养".equals(project.getCollaborationType())) {
            throw new RuntimeException("只有订单培养项目才能提交步骤");
        }

        // 检查当前项目步骤
        if (project.getCurrentStep() < stepNumber) {
            throw new RuntimeException("当前项目还未进入第" + stepNumber + "步");
        }

        // 检查是否已经提交过该步骤
        Optional<OrderTrainingStep> existingStepOpt = orderTrainingStepRepository.findByProjectIdAndStepNumber(projectId, stepNumber);

        OrderTrainingStep step;
        if (existingStepOpt.isPresent()) {
            // 更新现有步骤
            step = existingStepOpt.get();
            step.setUserId(userId);
            step.setUserRole(userRole);
            step.setTitle(title);
            step.setContent(content);
            step.setFileUrl(fileUrl);
            step.setSubmissionTime(LocalDateTime.now());
        } else {
            // 创建新步骤
            step = new OrderTrainingStep();
            step.setProjectId(projectId);
            step.setUserId(userId);
            step.setUserRole(userRole);
            step.setStepNumber(stepNumber);
            step.setTitle(title);
            step.setContent(content);
            step.setFileUrl(fileUrl);
            step.setSubmissionTime(LocalDateTime.now());
        }

        step = orderTrainingStepRepository.save(step);

        // 如果是企业用户且不是最后一步，自动更新项目步骤
        if ("企业".equals(userRole) && stepNumber < 4) {
            project.setCurrentStep(stepNumber + 1);
            projectRepository.save(project);
        }

        // 转换为DTO
        OrderTrainingStepDTO dto = new OrderTrainingStepDTO();
        dto.setId(step.getId());
        dto.setProjectId(step.getProjectId());
        dto.setUserId(step.getUserId());
        dto.setUserRole(step.getUserRole());
        dto.setStepNumber(step.getStepNumber());
        dto.setTitle(step.getTitle());
        dto.setContent(step.getContent());
        dto.setFileUrl(step.getFileUrl());
        dto.setSubmissionTime(step.getSubmissionTime());

        // 获取用户信息
        if ("学生".equals(userRole)) {
            studentRepository.findById(userId).ifPresent(student -> {
                dto.setUserName(student.getName());
                dto.setUserSchool(student.getSchool());
            });
        } else if ("企业".equals(userRole)) {
            enterpriseRepository.findById(userId).ifPresent(enterprise -> {
                dto.setUserName(enterprise.getName());
            });
        }

        return dto;
    }

    // 获取订单培养步骤
    public OrderTrainingStepDTO getOrderTrainingStep(Long projectId, Integer stepNumber) {
        Optional<OrderTrainingStep> stepOpt = orderTrainingStepRepository.findByProjectIdAndStepNumber(projectId, stepNumber);

        return stepOpt.map(step -> {
            OrderTrainingStepDTO dto = new OrderTrainingStepDTO();
            dto.setId(step.getId());
            dto.setProjectId(step.getProjectId());
            dto.setUserId(step.getUserId());
            dto.setUserRole(step.getUserRole());
            dto.setStepNumber(step.getStepNumber());
            dto.setTitle(step.getTitle());
            dto.setContent(step.getContent());
            dto.setFileUrl(step.getFileUrl());
            dto.setSubmissionTime(step.getSubmissionTime());

            // 获取用户信息
            if ("学生".equals(step.getUserRole())) {
                studentRepository.findById(step.getUserId()).ifPresent(student -> {
                    dto.setUserName(student.getName());
                    dto.setUserSchool(student.getSchool());
                });
            } else if ("企业".equals(step.getUserRole())) {
                enterpriseRepository.findById(step.getUserId()).ifPresent(enterprise -> {
                    dto.setUserName(enterprise.getName());
                });
            }

            return dto;
        }).orElse(null);
    }
}
