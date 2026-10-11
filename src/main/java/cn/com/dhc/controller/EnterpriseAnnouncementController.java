package cn.com.dhc.controller;

import cn.com.dhc.Entity.Notice;
import cn.com.dhc.Entity.AnnouncementStatus;
import cn.com.dhc.service.NoticeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enterprises/{enterpriseId}/announcements")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // 添加CORS支持
public class EnterpriseAnnouncementController {
    private final NoticeService announcementService;

    @PostMapping
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseEntity<Notice> createAnnouncement(
            @PathVariable Long enterpriseId,
            @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(
                announcementService.createAnnouncement(
                        enterpriseId,
                        request.getTitle(),
                        request.getContent(),
                        AnnouncementStatus.PENDING // 直接发布为待审核状态
                )
        );
    }

    @PostMapping("/draft")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseEntity<Notice> saveDraft(
            @PathVariable Long enterpriseId,
            @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(
                announcementService.createAnnouncement(
                        enterpriseId,
                        request.getTitle(),
                        request.getContent(),
                        AnnouncementStatus.DRAFT // 保存为草稿状态
                )
        );
    }

    @GetMapping("/draft")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseEntity<Notice> getDraft(
            @PathVariable Long enterpriseId
    ) {
        Optional<Notice> draft = announcementService.getEnterpriseDraft(enterpriseId);
        return draft.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/draft/{noticeId}")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseEntity<Notice> updateDraft(
            @PathVariable Long enterpriseId,
            @PathVariable Long noticeId,
            @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(
                announcementService.updateDraft(
                        enterpriseId,
                        noticeId,
                        request.getTitle(),
                        request.getContent()
                )
        );
    }

    @PostMapping("/draft/{noticeId}/submit")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseEntity<Notice> submitDraftForReview(
            @PathVariable Long enterpriseId,
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(
                announcementService.submitDraftForReview(enterpriseId, noticeId)
        );
    }

    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long enterpriseId,
            @PathVariable Long noticeId
    ) {
        announcementService.deleteAnnouncement(enterpriseId, noticeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseEntity<List<Notice>> getEnterpriseAnnouncements(
            @PathVariable Long enterpriseId
    ) {
        return ResponseEntity.ok(
                announcementService.getEnterpriseAnnouncements(enterpriseId)
        );
    }
}

@Data
class AnnouncementRequest {
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;
}
