package cn.com.dhc.service;

import cn.com.dhc.Entity.Notice;
import cn.com.dhc.Entity.AnnouncementStatus;
import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.mapper.NoticeRepository;
import cn.com.dhc.mapper.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final EnterpriseRepository enterpriseRepository;

    // 企业创建公告
    @Transactional
    public Notice createAnnouncement(Long enterpriseId, String title, String content, AnnouncementStatus status) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("企业不存在"));

        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setEnterprise(enterprise);
        notice.setStatus(status); // 设置指定状态
        notice.setCreatedTime(LocalDateTime.now()); // 设置创建时间
        return noticeRepository.save(notice);
    }

    // 兼容旧版本的方法
    @Transactional
    public Notice createAnnouncement(Long enterpriseId, String title, String content) {
        return createAnnouncement(enterpriseId, title, content, AnnouncementStatus.PENDING);
    }

    // 获取企业的草稿公告
    public Optional<Notice> getEnterpriseDraft(Long enterpriseId) {
        List<Notice> drafts = noticeRepository.findByEnterpriseIdAndStatus(
                enterpriseId, AnnouncementStatus.DRAFT);
        // 返回最新的草稿
        return drafts.stream()
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .findFirst();
    }

    // 更新草稿
    @Transactional
    public Notice updateDraft(Long enterpriseId, Long noticeId, String title, String content) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        // 验证公告属于该企业且状态为草稿
        if (!notice.getEnterprise().getEnterpriseId().equals(enterpriseId)) {
            throw new RuntimeException("无权操作该公告");
        }

        if (notice.getStatus() != AnnouncementStatus.DRAFT) {
            throw new RuntimeException("只能更新草稿状态的公告");
        }

        notice.setTitle(title);
        notice.setContent(content);
        notice.setCreatedTime(LocalDateTime.now()); // 更新创建时间

        return noticeRepository.save(notice);
    }

    // 提交草稿进行审核
    @Transactional
    public Notice submitDraftForReview(Long enterpriseId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        // 验证公告属于该企业且状态为草稿
        if (!notice.getEnterprise().getEnterpriseId().equals(enterpriseId)) {
            throw new RuntimeException("无权操作该公告");
        }

        if (notice.getStatus() != AnnouncementStatus.DRAFT && notice.getStatus() != AnnouncementStatus.REJECTED) {
            throw new RuntimeException("只能提交草稿或被拒绝状态的公告");
        }

        // 更新状态为待审核
        notice.setStatus(AnnouncementStatus.PENDING);
        notice.setCreatedTime(LocalDateTime.now()); // 更新创建时间

        return noticeRepository.save(notice);
    }

    // 删除公告
    @Transactional
    public void deleteAnnouncement(Long enterpriseId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        // 验证公告属于该企业
        if (!notice.getEnterprise().getEnterpriseId().equals(enterpriseId)) {
            throw new RuntimeException("无权操作该公告");
        }

        noticeRepository.delete(notice);
    }

    // 提交审核
    @Transactional
    public Notice submitForReview(Long announcementId) {
        Notice notice = noticeRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        if (notice.getStatus() != AnnouncementStatus.DRAFT) {
            throw new IllegalStateException("非草稿状态不可提交审核");
        }

        notice.setStatus(AnnouncementStatus.PENDING);
        return noticeRepository.save(notice);
    }

    // 管理员审核
    @Transactional
    public Notice reviewAnnouncement(Long announcementId, boolean approved, String feedback) {
        Notice notice = noticeRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        if (notice.getStatus() != AnnouncementStatus.PENDING) {
            throw new IllegalStateException("当前状态不可审核");
        }

        notice.setStatus(approved ? AnnouncementStatus.APPROVED : AnnouncementStatus.REJECTED);
        notice.setFeedback(feedback);
        notice.setReviewedTime(LocalDateTime.now());
        return noticeRepository.save(notice);
    }

    public List<Notice> findAllNotice(){
        return noticeRepository.findAll();
    }

    public Notice findNotice(Long noticeId) {
        return noticeRepository.findByNoticeId(noticeId);
    }

    public Page<Notice> findAll(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    public List<Notice> findByStatus(AnnouncementStatus status) {
        List<Notice> notices = noticeRepository.findByStatus(status);
        System.out.println("Found " + notices.size() + " notices with status " + status);
        return notices;
    }

    // 获取企业发布的所有公告
    public List<Notice> getEnterpriseAnnouncements(Long enterpriseId) {
        return noticeRepository.findByEnterpriseId(enterpriseId);
    }

    public Page<Notice> findAllPublished(Pageable pageable) {
        return noticeRepository.findByStatus(AnnouncementStatus.APPROVED, pageable);
    }

    public List<Notice> findPendingNotices() {
        return noticeRepository.findByStatus(AnnouncementStatus.PENDING);
    }

//    public Page<Notice> findAll(Pageable pageable) {
//        return noticeRepository.findAll(pageable);
//    }
}
