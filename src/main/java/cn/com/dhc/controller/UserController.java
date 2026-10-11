package cn.com.dhc.controller;

import cn.com.dhc.Entity.*;
import cn.com.dhc.mapper.*;
import cn.com.dhc.Entity.AnnouncementStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class UserController {
    private final StudentRepository studentRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final AdminRepository adminRepository;
    private final TaskRepository taskRepository;
    private final NoticeRepository noticeRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;

    // 学生登录
    @PostMapping("/student")
    public ResponseEntity<?> loginStudent(@RequestBody LoginRequest request) {
        Student student = studentRepository.findByEmail(request.getEmail());
        if (student != null && student.getPassword().equals(request.getPassword())) {
            return ResponseEntity.ok(student);  // 返回学生信息，登录成功
        }
        return ResponseEntity.badRequest().body(new LoginResponse(false, "邮箱或密码错误"));
    }

    // 企业登录
    @PostMapping("/enterprise")
    public ResponseEntity<?> loginEnterprise(@RequestBody LoginRequest request) {
        Enterprise enterprise = enterpriseRepository.findByEmail(request.getEmail());
        if (enterprise != null && enterprise.getPassword().equals(request.getPassword())) {
            return ResponseEntity.ok(enterprise);  // 返回企业信息，登录成功
        }
        return ResponseEntity.badRequest().body(new LoginResponse(false, "邮箱或密码错误"));
    }

    @GetMapping("/notice/all")
    public ResponseEntity<List<Notice>> findAllNotice()
    {
        // 只返回已审核通过的公告
        List<Notice> noticeList = noticeRepository.findByStatus(AnnouncementStatus.APPROVED);
        return ResponseEntity.ok(noticeList);
    }

    // 管理员登录
    @PostMapping("/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail());
        if (admin != null && admin.getPassword().equals(request.getPassword())) {
            return ResponseEntity.ok(admin);  // 返回管理员信息，登录成功
        }
        return ResponseEntity.badRequest().body(new LoginResponse(false, "邮箱或密码错误"));
    }

    // 获取学生的最近5个任务
    @GetMapping("/student/{id}/tasks")
    public ResponseEntity<List<Task>> getStudentTasks(@PathVariable Long id) {
        List<Task> tasks = taskSubmissionRepository.findRecentTasksByStudent_StudentId(id);  // 获取最近的五个任务
        return ResponseEntity.ok(tasks);
    }

    // 获取学生的公告
    @GetMapping("/student/notices")
    public ResponseEntity<List<Notice>> getStudentNotices() {
        List<Notice> notices = noticeRepository.findRecentNotices();  // 获取最新的公告
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable Long studentId) {
        Student student = studentRepository.findByStudentId(studentId);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<Enterprise> getEnterprise(@PathVariable Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findByEnterpriseId(enterpriseId);
        if (enterprise == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(enterprise);
    }

    // LoginRequest and LoginResponse are simple DTOs for login
    public static class LoginRequest {
        @Getter
        @Setter
        private String email;
        @Getter
        @Setter
        private String password;

        // getters and setters
    }

    public static class LoginResponse {

        @Getter
        private boolean success;

        @Setter
        private String message;

        public LoginResponse(boolean b, String s) {
            success = b;
            message = s;
        }

        public String getMessage() {
            return message;
        }

        // Constructor, getters and setters
    }
}
