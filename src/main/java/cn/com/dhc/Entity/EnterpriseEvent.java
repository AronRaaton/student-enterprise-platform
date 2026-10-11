package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enterprise_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String location;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(name = "max_participants")
    private Integer maxParticipants = 100;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(nullable = false)
    private String status = "UPCOMING";

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations = new ArrayList<>();

    // 枚举类型定义活动类型
    public enum EventType {
        CAMPUS_VISIT("校园参观"),
        ANNIVERSARY("周年庆典"),
        WORKSHOP("工作坊"),
        SEMINAR("研讨会"),
        CAREER_FAIR("招聘会"),
        OTHER("其他活动");

        private final String displayName;

        EventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 枚举类型定义活动状态
    public enum EventStatus {
        UPCOMING("即将开始"),
        ONGOING("进行中"),
        COMPLETED("已结束"),
        CANCELLED("已取消");

        private final String displayName;

        EventStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
