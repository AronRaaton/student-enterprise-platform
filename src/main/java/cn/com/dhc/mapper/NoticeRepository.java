package cn.com.dhc.mapper;

import cn.com.dhc.Entity.Notice;
import cn.com.dhc.Entity.AnnouncementStatus;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 获取企业所有公告
    List<Notice> findByEnterprise_EnterpriseId(Long enterpriseId);

    // 获取待审核公告
    List<Notice> findByStatus(AnnouncementStatus status);

    // 统计企业公告数量
    @Query("SELECT COUNT(a) FROM Notice a WHERE a.enterprise.enterpriseId = :enterpriseId")
    long countByEnterprise(@Param("enterpriseId") Long enterpriseId);

    @Query(value = "SELECT '*' FROM Notice\n" +
            "ORDER BY created_time DESC\n" +
            "LIMIT 5" , nativeQuery = true)
    List<Notice> findRecentNotices();

    Notice findByNoticeId(Long noticeId);

    // 获取指定企业的所有公告
    @Query("SELECT n FROM Notice n WHERE n.enterprise.enterpriseId = :enterpriseId ORDER BY n.createdTime DESC")
    List<Notice> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    // 获取指定企业的指定状态的公告
    @Query("SELECT n FROM Notice n WHERE n.enterprise.enterpriseId = :enterpriseId AND n.status = :status ORDER BY n.createdTime DESC")
    List<Notice> findByEnterpriseIdAndStatus(@Param("enterpriseId") Long enterpriseId, @Param("status") AnnouncementStatus status);

    Page<Notice> findByStatus(AnnouncementStatus status, Pageable pageable);
}
