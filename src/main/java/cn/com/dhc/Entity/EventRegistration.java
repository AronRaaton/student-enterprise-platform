package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private EnterpriseEvent event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "REGISTERED";

    // 枚举类型定义报名状态
    public enum RegistrationStatus {
        REGISTERED("已报名"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        ATTENDED("已参加"),
        ABSENT("缺席"),
        WITHDRAWN("已退出");

        private final String displayName;

        RegistrationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
