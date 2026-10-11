package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "competition")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "competition_id")
    private Long competitionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDate registrationDeadline;

    @Column(name = "max_participants")
    private Integer maxParticipants = 100;

    @Column(columnDefinition = "TEXT")
    private String rewards;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(name = "winners_selected")
    private Boolean winnersSelected = false;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "first_prize_submission_id")
    private CompetitionSubmission firstPrizeSubmission;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "second_prize_submission_id")
    private CompetitionSubmission secondPrizeSubmission;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "third_prize_submission_id")
    private CompetitionSubmission thirdPrizeSubmission;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CompetitionRegistration> registrations = new ArrayList<>();

    // 枚举类型定义竞赛状态
    public enum CompetitionStatus {
        OPEN("开放报名"),
        ONGOING("进行中"),
        CLOSED("已结束"),
        CANCELLED("已取消");

        private final String displayName;

        CompetitionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
