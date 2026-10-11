package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public enum AnnouncementStatus {
    DRAFT,      // 草稿状态
    PENDING,    // 待审核
    APPROVED,   // 审核通过
    REJECTED    // 已驳回
}