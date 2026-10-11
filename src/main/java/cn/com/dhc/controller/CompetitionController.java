package cn.com.dhc.controller;

import cn.com.dhc.Entity.*;
import cn.com.dhc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionRepository competitionRepository;
    private final CompetitionRegistrationRepository registrationRepository;
    private final CompetitionSubmissionRepository submissionRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final StudentRepository studentRepository;

    // 获取所有竞赛（分页）
    @GetMapping
    public ResponseEntity<Page<Competition>> getAllCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Competition> competitions = competitionRepository.findAll(pageable);

        // 自动更新竞赛状态
        LocalDate now = LocalDate.now();
        competitions.forEach(competition -> {
            String currentStatus = competition.getStatus();
            String newStatus = currentStatus;

            if (now.isBefore(competition.getStartDate())) {
                newStatus = "OPEN";
            } else if (now.isAfter(competition.getEndDate())) {
                newStatus = "CLOSED";
            } else {
                newStatus = "ONGOING";
            }

            // 如果状态发生变化，更新数据库
            if (!currentStatus.equals(newStatus)) {
                competition.setStatus(newStatus);
                competitionRepository.save(competition);
            }
        });

        return ResponseEntity.ok(competitions);
    }

    // 获取特定竞赛详情
    @GetMapping("/{competitionId}")
    public ResponseEntity<Competition> getCompetition(@PathVariable Long competitionId) {
        return competitionRepository.findById(competitionId)
                .map(competition -> {
                    // 自动更新竞赛状态
                    LocalDate now = LocalDate.now();
                    String currentStatus = competition.getStatus();
                    String newStatus = currentStatus;

                    if (now.isBefore(competition.getStartDate())) {
                        newStatus = "OPEN";
                    } else if (now.isAfter(competition.getEndDate())) {
                        newStatus = "CLOSED";
                    } else {
                        newStatus = "ONGOING";
                    }

                    // 如果状态发生变化，更新数据库
                    if (!currentStatus.equals(newStatus)) {
                        competition.setStatus(newStatus);
                        competitionRepository.save(competition);
                    }

                    return ResponseEntity.ok(competition);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 企业创建竞赛
    @PostMapping
    public ResponseEntity<Competition> createCompetition(@RequestBody Competition competition, @RequestParam Long enterpriseId) {
        return enterpriseRepository.findById(enterpriseId)
                .map(enterprise -> {
                    competition.setEnterprise(enterprise);
                    competition.setCreateTime(LocalDateTime.now());
                    competition.setUpdateTime(LocalDateTime.now());

                    // 根据日期自动设置状态
                    LocalDate now = LocalDate.now();
                    if (now.isBefore(competition.getStartDate())) {
                        competition.setStatus("OPEN");
                    } else if (now.isAfter(competition.getEndDate())) {
                        competition.setStatus("CLOSED");
                    } else {
                        competition.setStatus("ONGOING");
                    }

                    return ResponseEntity.ok(competitionRepository.save(competition));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    // 更新竞赛信息
    @PutMapping("/{competitionId}")
    public ResponseEntity<Competition> updateCompetition(
            @PathVariable Long competitionId,
            @RequestBody Competition competitionDetails) {
        return competitionRepository.findById(competitionId)
                .map(competition -> {
                    competition.setTitle(competitionDetails.getTitle());
                    competition.setDescription(competitionDetails.getDescription());
                    competition.setStartDate(competitionDetails.getStartDate());
                    competition.setEndDate(competitionDetails.getEndDate());
                    competition.setRegistrationDeadline(competitionDetails.getRegistrationDeadline());
                    competition.setMaxParticipants(competitionDetails.getMaxParticipants());
                    competition.setRewards(competitionDetails.getRewards());
                    competition.setRequirements(competitionDetails.getRequirements());
                    competition.setUpdateTime(LocalDateTime.now());

                    // 根据日期自动更新状态
                    LocalDate now = LocalDate.now();
                    if (now.isBefore(competition.getStartDate())) {
                        competition.setStatus("OPEN");
                    } else if (now.isAfter(competition.getEndDate())) {
                        competition.setStatus("CLOSED");
                    } else {
                        competition.setStatus("ONGOING");
                    }

                    return ResponseEntity.ok(competitionRepository.save(competition));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 删除竞赛
    @DeleteMapping("/{competitionId}")
    public ResponseEntity<Void> deleteCompetition(@PathVariable Long competitionId) {
        return competitionRepository.findById(competitionId)
                .map(competition -> {
                    competitionRepository.delete(competition);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 学生报名竞赛
    @PostMapping("/{competitionId}/register")
    public ResponseEntity<?> registerForCompetition(
            @PathVariable Long competitionId,
            @RequestParam Long studentId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (!competitionOpt.isPresent() || !studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body("竞赛或学生不存在");
        }

        Competition competition = competitionOpt.get();
        Student student = studentOpt.get();

        // 检查竞赛是否开放报名
        if (!"OPEN".equals(competition.getStatus())) {
            return ResponseEntity.badRequest().body("竞赛不在报名阶段");
        }

        // 检查是否已经报名
        if (registrationRepository.findByCompetitionCompetitionIdAndStudentStudentId(competitionId, studentId).isPresent()) {
            return ResponseEntity.badRequest().body("已经报名过该竞赛");
        }

        // 检查报名人数是否已满
        List<CompetitionRegistration> registrations = registrationRepository.findByCompetitionCompetitionId(competitionId);
        if (registrations.size() >= competition.getMaxParticipants()) {
            return ResponseEntity.badRequest().body("报名人数已满");
        }

        // 创建报名记录
        CompetitionRegistration registration = new CompetitionRegistration();
        registration.setCompetition(competition);
        registration.setStudent(student);
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);

        return ResponseEntity.ok().body("报名成功");
    }

    // 获取竞赛的所有报名记录
    @GetMapping("/{competitionId}/registrations")
    public ResponseEntity<List<CompetitionRegistration>> getCompetitionRegistrations(@PathVariable Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registrationRepository.findByCompetitionCompetitionId(competitionId));
    }

    // 学生提交竞赛作品
    @PostMapping("/submissions")
    public ResponseEntity<?> submitCompetitionWork(
            @RequestParam Long registrationId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String fileUrl,
            @RequestParam(required = false) MultipartFile file) {

        Optional<CompetitionRegistration> registrationOpt = registrationRepository.findById(registrationId);

        if (!registrationOpt.isPresent()) {
            return ResponseEntity.badRequest().body("报名记录不存在");
        }

        CompetitionRegistration registration = registrationOpt.get();
        Competition competition = registration.getCompetition();

        // 不再检查竞赛状态，允许学生在任何状态下提交作品

        // 创建提交记录
        CompetitionSubmission submission = new CompetitionSubmission();
        submission.setRegistration(registration);
        submission.setSubmissionTime(LocalDateTime.now());
        submission.setContent(content);
        submission.setFileUrl(fileUrl);
        submission.setStatus("SUBMITTED");

        // 处理文件上传
        if (file != null && !file.isEmpty()) {
            try {
                // 创建上传目录
                String uploadDir = "uploads/competitions/" + competition.getCompetitionId() + "/" + registration.getRegistrationId();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 保存文件
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);

                submission.setUploadedFile(fileName);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件上传失败: " + e.getMessage());
            }
        }

        submissionRepository.save(submission);

        return ResponseEntity.ok().body("作品提交成功");
    }

    // 企业评分竞赛作品
    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestParam(required = false) String feedback,
            @RequestParam(required = false) Double score) {

        return submissionRepository.findById(submissionId)
                .map(submission -> {
                    if (feedback != null) {
                        submission.setFeedback(feedback);
                    }

                    if (score != null) {
                        submission.setScore(java.math.BigDecimal.valueOf(score));
                    }

                    submission.setStatus("GRADED");
                    submissionRepository.save(submission);

                    return ResponseEntity.ok().body("评分成功");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 获取学生参与的竞赛
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Competition>> getStudentCompetitions(@PathVariable Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            return ResponseEntity.notFound().build();
        }

        List<Competition> competitions = competitionRepository.findCompetitionsByStudentId(studentId);

        // 自动更新竞赛状态
        LocalDate now = LocalDate.now();
        competitions.forEach(competition -> {
            String currentStatus = competition.getStatus();
            String newStatus = currentStatus;

            if (now.isBefore(competition.getStartDate())) {
                newStatus = "OPEN";
            } else if (now.isAfter(competition.getEndDate())) {
                newStatus = "CLOSED";
            } else {
                newStatus = "ONGOING";
            }

            // 如果状态发生变化，更新数据库
            if (!currentStatus.equals(newStatus)) {
                competition.setStatus(newStatus);
                competitionRepository.save(competition);
            }
        });

        return ResponseEntity.ok(competitions);
    }

    // 获取企业发布的竞赛
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<List<Competition>> getEnterpriseCompetitions(@PathVariable Long enterpriseId) {
        if (!enterpriseRepository.existsById(enterpriseId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(competitionRepository.findByEnterpriseEnterpriseId(enterpriseId));
    }

    // 获取特定报名记录的所有提交
    @GetMapping("/submissions/{registrationId}")
    public ResponseEntity<List<CompetitionSubmission>> getRegistrationSubmissions(@PathVariable Long registrationId) {
        if (!registrationRepository.existsById(registrationId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submissionRepository.findByRegistrationRegistrationId(registrationId));
    }

    // 获取竞赛的所有提交作品
    @GetMapping("/{competitionId}/submissions")
    public ResponseEntity<List<CompetitionSubmission>> getAllCompetitionSubmissions(@PathVariable Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            return ResponseEntity.notFound().build();
        }

        List<CompetitionRegistration> registrations = registrationRepository.findByCompetitionCompetitionId(competitionId);
        List<CompetitionSubmission> allSubmissions = new ArrayList<>();

        for (CompetitionRegistration registration : registrations) {
            allSubmissions.addAll(submissionRepository.findByRegistrationRegistrationId(registration.getRegistrationId()));
        }

        return ResponseEntity.ok(allSubmissions);
    }

    // 更新学生提交的作品
    @PutMapping("/submissions/{submissionId}")
    public ResponseEntity<?> updateSubmission(
            @PathVariable Long submissionId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String fileUrl,
            @RequestParam(required = false) MultipartFile file) {

        Optional<CompetitionSubmission> submissionOpt = submissionRepository.findById(submissionId);

        if (!submissionOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        CompetitionSubmission submission = submissionOpt.get();
        CompetitionRegistration registration = submission.getRegistration();
        Competition competition = registration.getCompetition();

        // 检查竞赛是否已结束或已评奖
        if ("CLOSED".equals(competition.getStatus()) && Boolean.TRUE.equals(competition.getWinnersSelected())) {
            return ResponseEntity.badRequest().body("竞赛已结束且已评奖，无法修改提交");
        }

        // 更新提交内容
        if (content != null) {
            submission.setContent(content);
        }

        if (fileUrl != null) {
            submission.setFileUrl(fileUrl);
        }

        // 处理文件上传
        if (file != null && !file.isEmpty()) {
            try {
                // 创建上传目录
                String uploadDir = "uploads/competitions/" + competition.getCompetitionId() + "/" + registration.getRegistrationId();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 保存文件
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                submission.setUploadedFile(fileName);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件上传失败: " + e.getMessage());
            }
        }

        submission.setSubmissionTime(LocalDateTime.now());
        submissionRepository.save(submission);

        return ResponseEntity.ok().body("作品更新成功");
    }

    // 设置竞赛获奖作品
    @PostMapping("/{competitionId}/winners")
    public ResponseEntity<?> setCompetitionWinners(
            @PathVariable Long competitionId,
            @RequestParam Long firstPrizeSubmissionId,
            @RequestParam(required = false) Long secondPrizeSubmissionId,
            @RequestParam(required = false) Long thirdPrizeSubmissionId) {

        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (!competitionOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Competition competition = competitionOpt.get();

        // 检查竞赛是否已结束
        if (!"CLOSED".equals(competition.getStatus())) {
            return ResponseEntity.badRequest().body("只有已结束的竞赛才能评奖");
        }

        // 设置一等奖
        Optional<CompetitionSubmission> firstPrizeOpt = submissionRepository.findById(firstPrizeSubmissionId);
        if (!firstPrizeOpt.isPresent()) {
            return ResponseEntity.badRequest().body("一等奖提交作品不存在");
        }
        competition.setFirstPrizeSubmission(firstPrizeOpt.get());

        // 设置二等奖（如果有）
        if (secondPrizeSubmissionId != null) {
            Optional<CompetitionSubmission> secondPrizeOpt = submissionRepository.findById(secondPrizeSubmissionId);
            if (secondPrizeOpt.isPresent()) {
                competition.setSecondPrizeSubmission(secondPrizeOpt.get());
            }
        }

        // 设置三等奖（如果有）
        if (thirdPrizeSubmissionId != null) {
            Optional<CompetitionSubmission> thirdPrizeOpt = submissionRepository.findById(thirdPrizeSubmissionId);
            if (thirdPrizeOpt.isPresent()) {
                competition.setThirdPrizeSubmission(thirdPrizeOpt.get());
            }
        }

        // 标记竞赛已评奖
        competition.setWinnersSelected(true);
        competitionRepository.save(competition);

        return ResponseEntity.ok().body("竞赛获奖作品设置成功");
    }

    // 获取竞赛获奖信息
    @GetMapping("/{competitionId}/winners")
    public ResponseEntity<?> getCompetitionWinners(@PathVariable Long competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (!competitionOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Competition competition = competitionOpt.get();

        if (!Boolean.TRUE.equals(competition.getWinnersSelected())) {
            return ResponseEntity.badRequest().body("该竞赛尚未评奖");
        }

        Map<String, Object> winnersInfo = new HashMap<>();
        winnersInfo.put("competitionId", competition.getCompetitionId());
        winnersInfo.put("competitionTitle", competition.getTitle());
        winnersInfo.put("winnersSelected", competition.getWinnersSelected());

        if (competition.getFirstPrizeSubmission() != null) {
            Map<String, Object> firstPrize = new HashMap<>();
            CompetitionSubmission submission = competition.getFirstPrizeSubmission();
            firstPrize.put("submissionId", submission.getSubmissionId());
            firstPrize.put("studentName", submission.getRegistration().getStudent().getName());
            firstPrize.put("submissionTime", submission.getSubmissionTime());
            firstPrize.put("content", submission.getContent());
            firstPrize.put("fileUrl", submission.getFileUrl());
            firstPrize.put("uploadedFile", submission.getUploadedFile());
            winnersInfo.put("firstPrize", firstPrize);
        }

        if (competition.getSecondPrizeSubmission() != null) {
            Map<String, Object> secondPrize = new HashMap<>();
            CompetitionSubmission submission = competition.getSecondPrizeSubmission();
            secondPrize.put("submissionId", submission.getSubmissionId());
            secondPrize.put("studentName", submission.getRegistration().getStudent().getName());
            secondPrize.put("submissionTime", submission.getSubmissionTime());
            secondPrize.put("content", submission.getContent());
            secondPrize.put("fileUrl", submission.getFileUrl());
            secondPrize.put("uploadedFile", submission.getUploadedFile());
            winnersInfo.put("secondPrize", secondPrize);
        }

        if (competition.getThirdPrizeSubmission() != null) {
            Map<String, Object> thirdPrize = new HashMap<>();
            CompetitionSubmission submission = competition.getThirdPrizeSubmission();
            thirdPrize.put("submissionId", submission.getSubmissionId());
            thirdPrize.put("studentName", submission.getRegistration().getStudent().getName());
            thirdPrize.put("submissionTime", submission.getSubmissionTime());
            thirdPrize.put("content", submission.getContent());
            thirdPrize.put("fileUrl", submission.getFileUrl());
            thirdPrize.put("uploadedFile", submission.getUploadedFile());
            winnersInfo.put("thirdPrize", thirdPrize);
        }

        return ResponseEntity.ok(winnersInfo);
    }
}
