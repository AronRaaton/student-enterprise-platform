package cn.com.dhc.controller;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.EnterpriseVerification;
import cn.com.dhc.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/enterprise-verifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminEnterpriseController {

    private final AuthService authService;

    /**
     * 获取待审核的企业验证申请
     */
    @GetMapping("/pending")
    public ResponseEntity<List<EnterpriseVerification>> getPendingVerifications() {
        List<EnterpriseVerification> pendingVerifications = authService.getPendingEnterpriseVerifications();
        return ResponseEntity.ok(pendingVerifications);
    }

    /**
     * 审核通过企业验证申请
     */
    @PostMapping("/{verificationId}/approve")
    public ResponseEntity<?> approveVerification(
            @PathVariable Long verificationId,
            @RequestParam Long adminId
    ) {
        try {
            EnterpriseVerification verification = authService.approveEnterpriseVerification(verificationId, adminId);
            return ResponseEntity.ok(verification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * 拒绝企业验证申请
     */
    @PostMapping("/{verificationId}/reject")
    public ResponseEntity<?> rejectVerification(
            @PathVariable Long verificationId,
            @RequestParam Long adminId
    ) {
        try {
            EnterpriseVerification verification = authService.rejectEnterpriseVerification(
                    verificationId, adminId
            );
            return ResponseEntity.ok(verification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }



    @Data
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
