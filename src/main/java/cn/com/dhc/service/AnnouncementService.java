package cn.com.dhc.service;

import cn.com.dhc.Entity.Announcement;
import cn.com.dhc.Entity.AnnouncementStatus;
import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.mapper.AnnouncementRepository;
import cn.com.dhc.mapper.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final EnterpriseRepository enterpriseRepository;

    // 企业创建公告
    @Transactional
    public Announcement createAnnouncement(Long enterpriseId, String title, String content) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("企业不存在"));

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setEnterprise(enterprise);
        return announcementRepository.save(announcement);
    }

    // 提交审核
    @Transactional
    public Announcement submitForReview(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        if (announcement.getStatus() != AnnouncementStatus.DRAFT) {
            throw new IllegalStateException("非草稿状态不可提交审核");
        }

        announcement.setStatus(AnnouncementStatus.PENDING);
        return announcementRepository.save(announcement);
    }

    // 管理员审核
    @Transactional
    public Announcement reviewAnnouncement(Long announcementId, boolean approved, String feedback) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        if (announcement.getStatus() != AnnouncementStatus.PENDING) {
            throw new IllegalStateException("当前状态不可审核");
        }

        announcement.setStatus(approved ? AnnouncementStatus.APPROVED : AnnouncementStatus.REJECTED);
        announcement.setFeedback(feedback);
        announcement.setReviewedTime(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }

    public List<Announcement> findByStatus(AnnouncementStatus pending) {
        return announcementRepository.findByStatus(pending);
    }
}