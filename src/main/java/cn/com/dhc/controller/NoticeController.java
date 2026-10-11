package cn.com.dhc.controller;

import cn.com.dhc.Entity.Notice;
import cn.com.dhc.service.NoticeService;
import cn.com.dhc.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;
    private final TaskService taskService;

    @GetMapping("/{noticeId}")
    public ResponseEntity<Notice> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.findNotice(noticeId));
    }
}
