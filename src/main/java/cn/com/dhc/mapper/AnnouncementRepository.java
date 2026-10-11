package cn.com.dhc.mapper;

import cn.com.dhc.Entity.Announcement;
import cn.com.dhc.Entity.AnnouncementStatus;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 获取企业所有公告
    List<Announcement> findByEnterprise_EnterpriseId(Long enterpriseId);

    // 获取待审核公告
    List<Announcement> findByStatus(AnnouncementStatus status);

    // 统计企业公告数量
    @Query("SELECT COUNT(a) FROM Announcement a WHERE a.enterprise.enterpriseId = :enterpriseId")
    long countByEnterprise(@Param("enterpriseId") Long enterpriseId);
}