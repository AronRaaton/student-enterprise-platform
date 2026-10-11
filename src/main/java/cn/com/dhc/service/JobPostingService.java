package cn.com.dhc.service;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.JobPosting;
import cn.com.dhc.dto.JobPostingDTO;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobPostingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 创建新岗位
    @Transactional
    public JobPostingDTO createJobPosting(JobPostingDTO jobPostingDTO) {
        Optional<Enterprise> enterpriseOpt = enterpriseRepository.findById(jobPostingDTO.getEnterpriseId());
        if (!enterpriseOpt.isPresent()) {
            throw new RuntimeException("企业不存在");
        }

        JobPosting jobPosting = new JobPosting();
        jobPosting.setEnterprise(enterpriseOpt.get());
        jobPosting.setTitle(jobPostingDTO.getTitle());
        jobPosting.setDescription(jobPostingDTO.getDescription());
        jobPosting.setCity(jobPostingDTO.getCity());
        // 根据最低和最高薪资生成薪资描述
        Integer minSalary = jobPostingDTO.getMinSalary();
        Integer maxSalary = jobPostingDTO.getMaxSalary();

        String salaryDesc;
        if (minSalary == null && maxSalary == null) {
            salaryDesc = "面议";
        } else if (minSalary != null && maxSalary != null && minSalary.equals(maxSalary)) {
            salaryDesc = formatSalary(minSalary);
        } else {
            salaryDesc = formatSalary(minSalary) + " - " + formatSalary(maxSalary);
        }


        jobPosting.setMinSalary(minSalary);
        jobPosting.setMaxSalary(maxSalary);
        jobPosting.setJobType(jobPostingDTO.getJobType());
        jobPosting.setRequirements(jobPostingDTO.getRequirements());
        jobPosting.setTechStack(jobPostingDTO.getTechStack());
        jobPosting.setPostTime(LocalDateTime.now());
        jobPosting.setStatus("OPEN");

        JobPosting savedJob = jobPostingRepository.save(jobPosting);
        return convertToDTO(savedJob);
    }

    // 更新岗位信息
    @Transactional
    public JobPostingDTO updateJobPosting(Long jobId, JobPostingDTO jobPostingDTO) {
        Optional<JobPosting> jobOpt = jobPostingRepository.findById(jobId);
        if (!jobOpt.isPresent()) {
            throw new RuntimeException("岗位不存在");
        }

        JobPosting jobPosting = jobOpt.get();
        jobPosting.setTitle(jobPostingDTO.getTitle());
        jobPosting.setDescription(jobPostingDTO.getDescription());
        jobPosting.setCity(jobPostingDTO.getCity());
        jobPosting.setJobType(jobPostingDTO.getJobType());
        jobPosting.setRequirements(jobPostingDTO.getRequirements());
        jobPosting.setMinSalary(jobPostingDTO.getMinSalary());
        jobPosting.setMaxSalary(jobPostingDTO.getMaxSalary());
        jobPosting.setTechStack(jobPostingDTO.getTechStack());
        // 不更新发布时间和企业信息

        JobPosting updatedJob = jobPostingRepository.save(jobPosting);
        return convertToDTO(updatedJob);
    }

    // 更改岗位状态（开放/关闭）
    @Transactional
    public JobPostingDTO updateJobStatus(Long jobId, String status) {
        Optional<JobPosting> jobOpt = jobPostingRepository.findById(jobId);
        if (!jobOpt.isPresent()) {
            throw new RuntimeException("岗位不存在");
        }

        JobPosting jobPosting = jobOpt.get();
        jobPosting.setStatus(status);

        JobPosting updatedJob = jobPostingRepository.save(jobPosting);
        return convertToDTO(updatedJob);
    }

    // 获取岗位详情
    public JobPostingDTO getJobPosting(Long jobId) {
        Optional<JobPosting> jobOpt = jobPostingRepository.findById(jobId);
        if (!jobOpt.isPresent()) {
            throw new RuntimeException("岗位不存在");
        }

        return convertToDTO(jobOpt.get());
    }

    // 获取企业的所有岗位
    public List<JobPostingDTO> getEnterpriseJobs(Long enterpriseId) {
        List<JobPosting> jobs = jobPostingRepository.findByEnterprise_EnterpriseId(enterpriseId);
        return jobs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取企业的开放岗位
    public List<JobPostingDTO> getEnterpriseOpenJobs(Long enterpriseId) {
        List<JobPosting> jobs = jobPostingRepository.findByEnterprise_EnterpriseIdAndStatus(enterpriseId, "OPEN");
        return jobs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 分页获取所有开放岗位
    public Page<JobPostingDTO> getAllOpenJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postTime"));
        Page<JobPosting> jobPage = jobPostingRepository.findByStatus("OPEN", pageable);

        return jobPage.map(this::convertToDTO);
    }

    // 根据城市和岗位类型筛选岗位
    public List<JobPostingDTO> filterJobs(String city, String jobType) {
        List<JobPosting> jobs;

        if (city != null && !city.isEmpty() && jobType != null && !jobType.isEmpty()) {
            jobs = jobPostingRepository.findByCityAndJobTypeAndStatus(city, jobType, "OPEN");
        } else if (city != null && !city.isEmpty()) {
            jobs = jobPostingRepository.findByCityAndStatus(city, "OPEN");
        } else if (jobType != null && !jobType.isEmpty()) {
            jobs = jobPostingRepository.findByJobTypeAndStatus(jobType, "OPEN");
        } else {
            jobs = jobPostingRepository.findByStatus("OPEN", PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "postTime"))).getContent();
        }

        return jobs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 搜索岗位
    public Page<JobPostingDTO> searchJobs(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postTime"));
        Page<JobPosting> jobPage = jobPostingRepository.searchJobs(keyword, "OPEN", pageable);

        return jobPage.map(this::convertToDTO);
    }

    // 高级筛选岗位
    public List<JobPostingDTO> advancedFilterJobs(String city, Integer minSalary, Integer maxSalary, String companySize, String jobType) {
        Integer minEmployeeCount = null;
        Integer maxEmployeeCount = null;

        // 根据企业规模设置员工数量范围
        if (companySize != null && !companySize.isEmpty()) {
            switch (companySize) {
                case "small":
                    minEmployeeCount = 0;
                    maxEmployeeCount = 999;
                    break;
                case "medium":
                    minEmployeeCount = 1000;
                    maxEmployeeCount = 4999;
                    break;
                case "large":
                    minEmployeeCount = 5000;
                    maxEmployeeCount = Integer.MAX_VALUE;
                    break;
            }
        }

        List<JobPosting> jobs = jobPostingRepository.advancedFilterJobs(
                city, minSalary, maxSalary, minEmployeeCount, maxEmployeeCount, jobType, "OPEN");

        return jobs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取相似岗位
    public List<JobPostingDTO> getSimilarJobs(Long jobId) {
        // 获取当前岗位
        Optional<JobPosting> jobOpt = jobPostingRepository.findById(jobId);
        if (!jobOpt.isPresent()) {
            return new ArrayList<>();
        }

        JobPosting job = jobOpt.get();

        // 根据当前岗位的城市和类型获取相似岗位
        List<JobPosting> similarJobs = jobPostingRepository.findByCityAndJobTypeAndStatus(
                job.getCity(), job.getJobType(), "OPEN");

        // 过滤掉当前岗位，并限制最多返回5个相似岗位
        return similarJobs.stream()
                .filter(j -> !j.getJobId().equals(jobId))
                .limit(5)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 格式化薪资
    private String formatSalary(Integer salary) {
        if (salary == null) {
            return "0";
        }

        // 如果薪资大于等于10000，显示为XXk
        if (salary >= 10000) {
            return (salary / 1000) + "k";
        }

        // 否则显示为XXXX元
        return salary + "元";
    }

    // 将实体转换为DTO
    private JobPostingDTO convertToDTO(JobPosting jobPosting) {
        JobPostingDTO dto = new JobPostingDTO();
        dto.setJobId(jobPosting.getJobId());
        dto.setEnterpriseId(jobPosting.getEnterprise().getEnterpriseId());
        dto.setEnterpriseName(jobPosting.getEnterprise().getName());
        dto.setTitle(jobPosting.getTitle());
        dto.setDescription(jobPosting.getDescription());
        dto.setCity(jobPosting.getCity());
        dto.setMinSalary(jobPosting.getMinSalary());
        dto.setMaxSalary(jobPosting.getMaxSalary());
        dto.setJobType(jobPosting.getJobType());
        dto.setRequirements(jobPosting.getRequirements());
        dto.setPostTime(jobPosting.getPostTime());
        dto.setStatus(jobPosting.getStatus());
        dto.setPositionTitle(jobPosting.getPositionTitle());
        dto.setEducationRequirement(jobPosting.getEducationRequirement());
        dto.setTechStack(jobPosting.getTechStack());

        // 格式化发布时间
        if (jobPosting.getPostTime() != null) {
            dto.setFormattedPostTime(jobPosting.getPostTime().format(formatter));
        }

        // 添加企业相关信息
        Enterprise enterprise = jobPosting.getEnterprise();
        if (enterprise != null) {
            dto.setEnterpriseType(enterprise.getType());
            dto.setEnterpriseLocation(enterprise.getLocation());
            dto.setEnterpriseEmployeeCount(enterprise.getEmployeeCount());
            dto.setEnterpriseWebsite(enterprise.getWebsite());
        }

        return dto;
    }
}
