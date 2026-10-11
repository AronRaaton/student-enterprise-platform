package cn.com.dhc.controller;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.EnterpriseVerification;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // 添加CORS支持
public class AuthController {
    private final AuthService authService;



    /**
     * 学生注册
     */
    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegisterRequest request) {
        try {
            Student student = authService.registerStudent(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName(),
                    request.getVerificationCode(),
                    request.getSchool(),
                    request.getStudentNumber(),
                    request.getMajor(),
                    request.getEnrollmentYear()
            );
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new RegisterResponse(false, e.getMessage()));
        }
    }

    /**
     * 企业注册
     */
    @PostMapping("/register/enterprise")
    public ResponseEntity<?> registerEnterprise(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("legalRepresentative") String legalRepresentative,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("businessLicense") MultipartFile businessLicense
    ) {
        try {
            System.out.println("收到企业注册请求：" + email + ", " + name);
            System.out.println("法定代表人：" + legalRepresentative);
            System.out.println("上传的文件名：" + businessLicense.getOriginalFilename());
            System.out.println("文件大小：" + businessLicense.getSize() + " 字节");

            // 验证文件是否为空
            if (businessLicense.isEmpty()) {
                return ResponseEntity.badRequest().body(new RegisterResponse(false, "营业执照文件不能为空"));
            }

            EnterpriseVerification verification = authService.registerEnterpriseWithVerification(
                    email, password, name, legalRepresentative, description, businessLicense
            );

            System.out.println("企业注册成功，验证ID：" + verification.getVerificationId());
            return ResponseEntity.ok(verification);
        } catch (IOException e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new RegisterResponse(false, "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("企业注册失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new RegisterResponse(false, e.getMessage()));
        }
    }

    /**
     * 提交企业验证
     */
    @PostMapping("/enterprise/verify")
    public ResponseEntity<?> submitEnterpriseVerification(
            @RequestParam("enterpriseId") Long enterpriseId,
            @RequestParam("legalRepresentative") String legalRepresentative,
            @RequestParam("businessLicense") MultipartFile businessLicense
    ) {
        try {
            EnterpriseVerification verification = authService.submitEnterpriseVerification(
                    enterpriseId, legalRepresentative, businessLicense
            );
            return ResponseEntity.ok(verification);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new RegisterResponse(false, "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new RegisterResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取企业验证状态
     */
    @GetMapping("/enterprise/{enterpriseId}/verification-status")
    public ResponseEntity<?> getEnterpriseVerificationStatus(@PathVariable Long enterpriseId) {
        try {
            EnterpriseVerification verification = authService.getEnterpriseVerificationStatus(enterpriseId);
            return ResponseEntity.ok(verification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new RegisterResponse(false, e.getMessage()));
        }
    }



    @Data
    public static class StudentRegisterRequest {
        private String email;
        private String password;
        private String name;
        private String verificationCode;
        private String school;
        private String studentNumber;
        private String major;
        private Integer enrollmentYear;
    }



    @Data
    public static class RegisterResponse {
        private boolean success;
        private String message;

        public RegisterResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
