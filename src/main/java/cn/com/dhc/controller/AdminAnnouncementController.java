package cn.com.dhc.controller;

import cn.com.dhc.Entity.Announcement;
import cn.com.dhc.Entity.AnnouncementStatus;
import cn.com.dhc.service.AnnouncementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {
    private final AnnouncementService announcementService;

    // 获取待审核公告
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Announcement>> getPendingAnnouncements() {
        return ResponseEntity.ok(
                announcementService.findByStatus(AnnouncementStatus.PENDING)
        );
    }

    // 审核公告
    @PostMapping("/{announcementId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Announcement> reviewAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementReviewRequest request
    ) {
        return ResponseEntity.ok(
                announcementService.reviewAnnouncement(
                        announcementId,
                        request.isApproved(),
                        request.getFeedback()
                )
        );
    }
}

@Data
class AnnouncementReviewRequest {
    private boolean approved;
    private String feedback;
}
