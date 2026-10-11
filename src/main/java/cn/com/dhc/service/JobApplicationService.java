package cn.com.dhc.service;

import cn.com.dhc.Entity.JobApplication;
import cn.com.dhc.Entity.JobPosting;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.dto.JobApplicationDTO;
import cn.com.dhc.dto.SkillDTO;
import cn.com.dhc.mapper.JobApplicationRepository;
import cn.com.dhc.mapper.JobPostingRepository;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.mapper.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobPostingRepository jobRepository;
    private final StudentRepository studentRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;

    // 学生申请岗位
    @Transactional
    public JobApplicationDTO applyForJob(Long jobId, Long studentId) {
        // 检查岗位是否存在
        Optional<JobPosting> jobOpt = jobRepository.findById(jobId);
        if (!jobOpt.isPresent()) {
            throw new RuntimeException("岗位不存在");
        }

        // 检查学生是否存在
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            throw new RuntimeException("学生不存在");
        }

        // 检查是否已经申请过
        Optional<JobApplication> existingApplication = applicationRepository.findByJob_JobIdAndStudent_StudentId(jobId, studentId);
        if (existingApplication.isPresent()) {
            throw new RuntimeException("您已经申请过该岗位");
        }

        // 创建新申请
        JobApplication application = new JobApplication();
        application.setJob(jobOpt.get());
        application.setStudent(studentOpt.get());
        application.setApplicationTime(LocalDateTime.now());
        application.setStatus("PENDING");

        JobApplication savedApplication = applicationRepository.save(application);
        return convertToDTO(savedApplication);
    }

    // 企业审核岗位申请
    @Transactional
    public JobApplicationDTO reviewApplication(Long applicationId, String status, String feedback) {
        Optional<JobApplication> applicationOpt = applicationRepository.findById(applicationId);
        if (!applicationOpt.isPresent()) {
            throw new RuntimeException("申请不存在");
        }

        JobApplication application = applicationOpt.get();
        application.setStatus(status);
        application.setFeedback(feedback);
        application.setReviewTime(LocalDateTime.now());

        JobApplication updatedApplication = applicationRepository.save(application);
        return convertToDTO(updatedApplication);
    }

    // 获取岗位的所有申请
    public List<JobApplicationDTO> getJobApplications(Long jobId) {
        List<JobApplication> applications = applicationRepository.findByJob_JobId(jobId);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取岗位的待审核申请
    public List<JobApplicationDTO> getPendingJobApplications(Long jobId) {
        List<JobApplication> applications = applicationRepository.findByJob_JobIdAndStatus(jobId, "PENDING");
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取学生的所有申请
    public List<JobApplicationDTO> getStudentApplications(Long studentId) {
        List<JobApplication> applications = applicationRepository.findByStudent_StudentId(studentId);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取企业的所有岗位申请
    public List<JobApplicationDTO> getEnterpriseApplications(Long enterpriseId) {
        List<JobApplication> applications = applicationRepository.findByEnterpriseId(enterpriseId);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取企业的待审核岗位申请
    public List<JobApplicationDTO> getEnterprisePendingApplications(Long enterpriseId) {
        List<JobApplication> applications = applicationRepository.findPendingByEnterpriseId(enterpriseId);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取申请详情
    public JobApplicationDTO getApplicationById(Long applicationId) {
        Optional<JobApplication> applicationOpt = applicationRepository.findById(applicationId);
        if (!applicationOpt.isPresent()) {
            throw new RuntimeException("申请不存在");
        }
        return convertToDTO(applicationOpt.get());
    }

    // 检查学生是否已申请岗位
    public boolean hasApplied(Long jobId, Long studentId) {
        return applicationRepository.findByJob_JobIdAndStudent_StudentId(jobId, studentId).isPresent();
    }

    // 转换为DTO
    private JobApplicationDTO convertToDTO(JobApplication application) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setApplicationId(application.getApplicationId());
        dto.setJobId(application.getJob().getJobId());
        dto.setJobTitle(application.getJob().getTitle());
        dto.setStudentId(application.getStudent().getStudentId());
        dto.setStudentName(application.getStudent().getName());
        dto.setStudentSchool(application.getStudent().getSchool());
        dto.setStudentMajor(application.getStudent().getMajor());
        dto.setApplicationTime(application.getApplicationTime());
        dto.setFormattedApplicationTime(application.getApplicationTime().format(formatter));
        dto.setStatus(application.getStatus());
        dto.setFeedback(application.getFeedback());
        dto.setReviewTime(application.getReviewTime());

        if (application.getReviewTime() != null) {
            dto.setFormattedReviewTime(application.getReviewTime().format(formatter));
        }

        // 获取学生技能数据
        dto.setSkills(getStudentSkills(application.getStudent().getStudentId()));

        return dto;
    }

    // 获取学生技能数据
    private List<SkillDTO> getStudentSkills(Long studentId) {
        // 获取学生的所有任务提交
        List<TaskSubmission> allSubmissions = taskSubmissionRepository.findByStudent_StudentId(studentId);

        // 过滤出已完成的任务
        List<TaskSubmission> completedTasks = allSubmissions.stream()
            .filter(submission -> submission.getStatus() != null &&
                    submission.getStatus().toString().equals("COMPLETED"))
            .collect(Collectors.toList());

        if (completedTasks.isEmpty()) {
            return new ArrayList<>();
        }

        // 技能映射表，用于累计每种技术的评分和次数
        Map<String, Map<String, Object>> skillMap = new HashMap<>();

        // 遍历所有已完成的任务
        for (TaskSubmission submission : completedTasks) {
            try {
                if (submission.getTask() == null || submission.getTask().getTechStack() == null) {
                    continue;
                }

                // 获取任务难度和评分
                Integer difficulty = submission.getTask().getDifficulty() != null ? submission.getTask().getDifficulty() : 3;
                Integer rating = submission.getRating() != null ? submission.getRating() : 3;

                // 分割技术栈字符串为数组
                String techStackString = submission.getTask().getTechStack();
                String[] techStacks = techStackString.split(",");

                for (String tech : techStacks) {
                    String techTrim = tech.trim();
                    if (techTrim.isEmpty()) continue;

                    if (!skillMap.containsKey(techTrim)) {
                        Map<String, Object> skillData = new HashMap<>();
                        skillData.put("total", 0.0);
                        skillData.put("count", 0);
                        skillMap.put(techTrim, skillData);
                    }

                    // 根据任务难度和评分计算技能值
                    double skillValue = (difficulty * 0.6 + rating * 0.4);

                    Map<String, Object> skillData = skillMap.get(techTrim);
                    skillData.put("total", (double) skillData.get("total") + skillValue);
                    skillData.put("count", (int) skillData.get("count") + 1);
                }
            } catch (Exception e) {
                // 忽略处理单个任务时的错误
                continue;
            }
        }

        // 转换为技能数组
        List<SkillDTO> skills = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : skillMap.entrySet()) {
            String techName = entry.getKey();
            Map<String, Object> skillData = entry.getValue();

            double total = (double) skillData.get("total");
            int count = (int) skillData.get("count");

            // 计算平均评分，并限制在1-5之间
            double avgRating = total / count;
            int level = Math.min(5, Math.max(1, (int) Math.round(avgRating)));

            SkillDTO skill = new SkillDTO();
            skill.setName(techName);
            skill.setLevel(level);
            skills.add(skill);
        }

        // 按评分降序排序，并限制最多显示8个技能
        skills.sort((a, b) -> b.getLevel() - a.getLevel());
        if (skills.size() > 8) {
            skills = skills.subList(0, 8);
        }

        return skills;
    }
}
