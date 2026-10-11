package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "technical_challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalChallenge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Integer difficulty = 3;
    
    @Column(name = "reward_description", columnDefinition = "TEXT")
    private String rewardDescription;
    
    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;
    
    private LocalDate deadline;
    
    @Column(nullable = false)
    private String status = "OPEN";
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TechnicalChallengeSubmission> submissions = new ArrayList<>();
    
    // 枚举类型定义挑战状态
    public enum ChallengeStatus {
        OPEN("开放中"),
        CLOSED("已关闭"),
        COMPLETED("已完成");
        
        private final String displayName;
        
        ChallengeStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
