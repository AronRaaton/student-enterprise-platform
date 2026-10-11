package cn.com.dhc.service;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.TechnicalChallenge;
import cn.com.dhc.Entity.TechnicalChallengeSubmission;
import cn.com.dhc.dto.TechnicalChallengeDTO;
import cn.com.dhc.dto.TechnicalChallengeSubmissionDTO;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.mapper.TechnicalChallengeRepository;
import cn.com.dhc.mapper.TechnicalChallengeSubmissionRepository;
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
public class TechnicalChallengeService {

    @Autowired
    private TechnicalChallengeRepository challengeRepository;
    
    @Autowired
    private TechnicalChallengeSubmissionRepository submissionRepository;
    
    @Autowired
    private EnterpriseRepository enterpriseRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 创建技术挑战
    @Transactional
    public TechnicalChallengeDTO createChallenge(TechnicalChallengeDTO challengeDTO) {
        Optional<Enterprise> enterpriseOpt = enterpriseRepository.findById(challengeDTO.getEnterpriseId());
        if (!enterpriseOpt.isPresent()) {
            throw new RuntimeException("企业不存在");
        }
        
        TechnicalChallenge challenge = new TechnicalChallenge();
        challenge.setEnterprise(enterpriseOpt.get());
        challenge.setTitle(challengeDTO.getTitle());
        challenge.setDescription(challengeDTO.getDescription());
        challenge.setDifficulty(challengeDTO.getDifficulty());
        challenge.setRewardDescription(challengeDTO.getRewardDescription());
        challenge.setRequiredSkills(challengeDTO.getRequiredSkills());
        challenge.setDeadline(challengeDTO.getDeadline());
        challenge.setStatus("OPEN");
        challenge.setCreateTime(LocalDateTime.now());
        
        TechnicalChallenge savedChallenge = challengeRepository.save(challenge);
        return convertToChallengeDTO(savedChallenge);
    }
    
    // 获取企业的所有技术挑战
    public List<TechnicalChallengeDTO> getEnterpriseChallenges(Long enterpriseId) {
        List<TechnicalChallenge> challenges = challengeRepository.findByEnterprise_EnterpriseId(enterpriseId);
        return challenges.stream()
                .map(this::convertToChallengeDTO)
                .collect(Collectors.toList());
    }
    
    // 获取企业的开放技术挑战
    public List<TechnicalChallengeDTO> getEnterpriseOpenChallenges(Long enterpriseId) {
        List<TechnicalChallenge> challenges = challengeRepository.findByEnterprise_EnterpriseIdAndStatus(enterpriseId, "OPEN");
        return challenges.stream()
                .map(this::convertToChallengeDTO)
                .collect(Collectors.toList());
    }
    
    // 分页获取所有开放技术挑战
    public Page<TechnicalChallengeDTO> getAllOpenChallenges(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TechnicalChallenge> challengePage = challengeRepository.findByStatus("OPEN", pageable);
        return challengePage.map(this::convertToChallengeDTO);
    }
    
    // 获取技术挑战详情
    public TechnicalChallengeDTO getChallengeById(Long challengeId) {
        Optional<TechnicalChallenge> challengeOpt = challengeRepository.findById(challengeId);
        if (!challengeOpt.isPresent()) {
            throw new RuntimeException("技术挑战不存在");
        }
        return convertToChallengeDTO(challengeOpt.get());
    }
    
    // 更新技术挑战状态
    @Transactional
    public TechnicalChallengeDTO updateChallengeStatus(Long challengeId, String status) {
        Optional<TechnicalChallenge> challengeOpt = challengeRepository.findById(challengeId);
        if (!challengeOpt.isPresent()) {
            throw new RuntimeException("技术挑战不存在");
        }
        
        TechnicalChallenge challenge = challengeOpt.get();
        challenge.setStatus(status);
        
        TechnicalChallenge updatedChallenge = challengeRepository.save(challenge);
        return convertToChallengeDTO(updatedChallenge);
    }
    
    // 提交技术挑战解决方案
    @Transactional
    public TechnicalChallengeSubmissionDTO submitSolution(Long challengeId, Long studentId, String solutionDescription, String solutionUrl, String uploadedFile) {
        Optional<TechnicalChallenge> challengeOpt = challengeRepository.findById(challengeId);
        if (!challengeOpt.isPresent()) {
            throw new RuntimeException("技术挑战不存在");
        }
        
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            throw new RuntimeException("学生不存在");
        }
        
        // 检查是否已经提交过
        Optional<TechnicalChallengeSubmission> existingSubmission = submissionRepository.findByChallenge_ChallengeIdAndStudent_StudentId(challengeId, studentId);
        if (existingSubmission.isPresent()) {
            throw new RuntimeException("您已经提交过该技术挑战的解决方案");
        }
        
        TechnicalChallengeSubmission submission = new TechnicalChallengeSubmission();
        submission.setChallenge(challengeOpt.get());
        submission.setStudent(studentOpt.get());
        submission.setSolutionDescription(solutionDescription);
        submission.setSolutionUrl(solutionUrl);
        submission.setUploadedFile(uploadedFile);
        submission.setStatus("PENDING");
        submission.setSubmitTime(LocalDateTime.now());
        
        TechnicalChallengeSubmission savedSubmission = submissionRepository.save(submission);
        return convertToSubmissionDTO(savedSubmission);
    }
    
    // 审核技术挑战提交
    @Transactional
    public TechnicalChallengeSubmissionDTO reviewSubmission(Long submissionId, String status, String feedback, Integer rating) {
        Optional<TechnicalChallengeSubmission> submissionOpt = submissionRepository.findById(submissionId);
        if (!submissionOpt.isPresent()) {
            throw new RuntimeException("提交不存在");
        }
        
        TechnicalChallengeSubmission submission = submissionOpt.get();
        submission.setStatus(status);
        submission.setFeedback(feedback);
        submission.setRating(rating);
        submission.setReviewTime(LocalDateTime.now());
        
        // 如果审核通过，增加学生的完成任务数
        if ("APPROVED".equals(status)) {
            Student student = submission.getStudent();
            student.setCompletedTasks(student.getCompletedTasks() + 1);
            studentRepository.save(student);
        }
        
        TechnicalChallengeSubmission updatedSubmission = submissionRepository.save(submission);
        return convertToSubmissionDTO(updatedSubmission);
    }
    
    // 获取技术挑战的所有提交
    public List<TechnicalChallengeSubmissionDTO> getChallengeSubmissions(Long challengeId) {
        List<TechnicalChallengeSubmission> submissions = submissionRepository.findByChallenge_ChallengeId(challengeId);
        return submissions.stream()
                .map(this::convertToSubmissionDTO)
                .collect(Collectors.toList());
    }
    
    // 获取技术挑战的待审核提交
    public List<TechnicalChallengeSubmissionDTO> getPendingSubmissions(Long challengeId) {
        List<TechnicalChallengeSubmission> submissions = submissionRepository.findByChallenge_ChallengeIdAndStatus(challengeId, "PENDING");
        return submissions.stream()
                .map(this::convertToSubmissionDTO)
                .collect(Collectors.toList());
    }
    
    // 获取学生的所有提交
    public List<TechnicalChallengeSubmissionDTO> getStudentSubmissions(Long studentId) {
        List<TechnicalChallengeSubmission> submissions = submissionRepository.findByStudent_StudentId(studentId);
        return submissions.stream()
                .map(this::convertToSubmissionDTO)
                .collect(Collectors.toList());
    }
    
    // 获取学生已完成的技术挑战
    public List<TechnicalChallengeSubmissionDTO> getStudentCompletedChallenges(Long studentId) {
        List<TechnicalChallengeSubmission> submissions = submissionRepository.findByStudent_StudentIdAndStatus(studentId, "APPROVED");
        return submissions.stream()
                .map(this::convertToSubmissionDTO)
                .collect(Collectors.toList());
    }
    
    // 将挑战实体转换为DTO
    private TechnicalChallengeDTO convertToChallengeDTO(TechnicalChallenge challenge) {
        TechnicalChallengeDTO dto = new TechnicalChallengeDTO();
        dto.setChallengeId(challenge.getChallengeId());
        dto.setEnterpriseId(challenge.getEnterprise().getEnterpriseId());
        dto.setEnterpriseName(challenge.getEnterprise().getName());
        dto.setTitle(challenge.getTitle());
        dto.setDescription(challenge.getDescription());
        dto.setDifficulty(challenge.getDifficulty());
        dto.setRewardDescription(challenge.getRewardDescription());
        dto.setRequiredSkills(challenge.getRequiredSkills());
        dto.setDeadline(challenge.getDeadline());
        dto.setStatus(challenge.getStatus());
        dto.setCreateTime(challenge.getCreateTime());
        dto.setFormattedCreateTime(challenge.getCreateTime().format(formatter));
        
        // 获取提交统计信息
        long submissionCount = submissionRepository.countByChallenge_ChallengeId(challenge.getChallengeId());
        long approvedCount = submissionRepository.countByChallenge_ChallengeIdAndStatus(challenge.getChallengeId(), "APPROVED");
        
        dto.setSubmissionCount((int) submissionCount);
        dto.setApprovedCount((int) approvedCount);
        
        return dto;
    }
    
    // 将提交实体转换为DTO
    private TechnicalChallengeSubmissionDTO convertToSubmissionDTO(TechnicalChallengeSubmission submission) {
        TechnicalChallengeSubmissionDTO dto = new TechnicalChallengeSubmissionDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setChallengeId(submission.getChallenge().getChallengeId());
        dto.setChallengeTitle(submission.getChallenge().getTitle());
        dto.setStudentId(submission.getStudent().getStudentId());
        dto.setStudentName(submission.getStudent().getName());
        dto.setStudentSchool(submission.getStudent().getSchool());
        dto.setSolutionDescription(submission.getSolutionDescription());
        dto.setSolutionUrl(submission.getSolutionUrl());
        dto.setUploadedFile(submission.getUploadedFile());
        dto.setStatus(submission.getStatus());
        dto.setSubmitTime(submission.getSubmitTime());
        dto.setFormattedSubmitTime(submission.getSubmitTime().format(formatter));
        
        if (submission.getReviewTime() != null) {
            dto.setReviewTime(submission.getReviewTime());
            dto.setFormattedReviewTime(submission.getReviewTime().format(formatter));
        }
        
        dto.setFeedback(submission.getFeedback());
        dto.setRating(submission.getRating());
        
        return dto;
    }
}
