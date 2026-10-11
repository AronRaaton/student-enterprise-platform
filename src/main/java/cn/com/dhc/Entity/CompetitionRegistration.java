package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "competition_registration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "competition_id")
    @JsonBackReference
    private Competition competition;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "REGISTERED";

    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CompetitionSubmission> submissions = new ArrayList<>();

    // 枚举类型定义报名状态
    public enum RegistrationStatus {
        REGISTERED("已报名"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
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
