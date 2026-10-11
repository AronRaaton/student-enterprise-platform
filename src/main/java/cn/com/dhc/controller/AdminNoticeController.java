package cn.com.dhc.controller;

import cn.com.dhc.Entity.Notice;
import cn.com.dhc.Entity.AnnouncementStatus;
import cn.com.dhc.service.NoticeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {
    private final NoticeService noticeService;

//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Page<Notice>> getNotices(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        return ResponseEntity.ok(noticeService.findAll(PageRequest.of(page, size)));
//    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Notice>> getNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(noticeService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notice>> getPendingAnnouncements() {
        List<Notice> pendingNotices = noticeService.findByStatus(AnnouncementStatus.PENDING);
        System.out.println("Found " + pendingNotices.size() + " pending notices"); // 调试日志
        return ResponseEntity.ok(pendingNotices);
    }

    @PostMapping("/{announcementId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notice> reviewAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody ReviewRequest request
    ) {
        return ResponseEntity.ok(
                noticeService.reviewAnnouncement(
                        announcementId,
                        request.isApproved(),
                        request.getFeedback()
                )
        );
    }
}



@Data
class ReviewRequest {
    private boolean approved;
    private String feedback;
}
